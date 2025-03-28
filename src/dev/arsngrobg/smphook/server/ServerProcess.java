package dev.arsngrobg.smphook.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import static dev.arsngrobg.smphook.SMPHookError.condition;

/**
 * <p>The {@code ServerProcess} class wraps a {@link java.lang.Process}, specifically running a Java Minecraft server process.</p>
 * 
 * <p>This class handles the I/O operations between SMPHook and the server.</p>
 * 
 * <p>A server process is highly customizable, allowing the use of experimental Java Virtual Machine (JVM) options using the {@link JVMOption} class.
 *    The minimum and maximum memory allocations can be defined using the {@link HeapArg} class.
 *    This provides a near 1:1 interface for creating a Minecraft: Java Edition process but through the Java environment.
 * </p>
 * 
 * <p>The underlying process is not initiated on object creation, but rather through the {@link #init(boolean)} method.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    HeapArg
 * @see    JVMOption
 * @see    MinecraftServer
 */
public final class ServerProcess {
    /**
     * <p>Constructs a {@code ServerProcess}.</p>
     * 
     * <p>This factory method validates the supplied {@code serverJar}.
     *    The {@code minHeap} and {@code maxHeap} arguments are <b>optional</b> and can be {@code null}.
     *    The {@link HeapArg}s cannot be mismatched or else an {@link SMPHookError} is thrown.
     *    You can supply a variable number of Java Virtual Machine (JVM) options to customize the Java runtime (see {@link JVMOption}).
     *    These arguments affect the result of the method {@link #getInitCommand()}, and subsequently the server itself.
     * </p>
     * 
     * @param serverJar - the path to the Minecraft: Java Edition server JAR file to be invoked
     * @param minHeap - the minimum allocation argument for the server
     * @param maxHeap - the maximum allocation argument for the server
     * @param options - a variable number of {@link JVMOption}s
     * @throws SMPHookError if {@code serverJar} is: {@code null}, doesn't exist, or not a file; the {@code minHeap} & {@code maxHeap} are mismatched
     * @return a new {@code ServerProcess} instance
     */
    public static ServerProcess spawn(String serverJar, HeapArg minHeap, HeapArg maxHeap, JVMOption... options) throws SMPHookError {
        File serverJarFile = SMPHookError.throwIfFail(() -> new File(serverJar));
        SMPHookError.caseThrow(
            condition(() -> !serverJarFile.exists(), SMPHookError.with(ErrorType.FILE, "serverJar does not exist.")),
            condition(() -> !serverJarFile.isFile(), SMPHookError.with(ErrorType.FILE, "serverJar is not a file.")),
            condition(() -> {
                String ext = serverJar.substring(serverJar.length() - 4, serverJar.length());
                return !ext.equals(".jar");
            }, SMPHookError.with(ErrorType.FILE, "serverJar file is not a .jar file."))
        );

        if ((minHeap != null && maxHeap != null) && minHeap.compareTo(maxHeap) == HeapArg.GREATER_THAN) {
            throw SMPHookError.withMessage("Mismatched heap arguments.");
        }

        options = SMPHookError.strictlyRequireNonNull(options, "options");

        return new ServerProcess(serverJarFile, minHeap, maxHeap, options);
    }

    /** <p>The string that represents the End Of File (EOF) character output by the server to declare that no more output will be made.</p> */
    public static final String EOF = "\0";

    // metadata
    private final File        serverJar;
    private final HeapArg     minHeap;
    private final HeapArg     maxHeap;
    private final JVMOption[] options;

    // state
    private Process        process;
    private BufferedWriter istream;
    private BufferedReader ostream;

    private ServerProcess(File serverJar, HeapArg minHeap, HeapArg maxHeap, JVMOption... options) {
        this.serverJar = serverJar;
        this.minHeap   = minHeap;
        this.maxHeap   = maxHeap;
        this.options   = options;
    }

    /**
     * <p>Initialises the server process.</p>
     * 
     * <p>If the server process is already running, an {@link SMPHookError} is thrown.</p>
     * 
     * <p>I/O operations can be performed on this process with {@link #rawInput(String) and {@link #rawOutput()}.</p>
     * 
     * @param nogui - whether to display the pre-packaged GUI on initialisation
     * @throws SMPHookError if the process is already running
     * @see #rawInput(String)
     * @see #rawOutput()
     */
    public void init(boolean nogui) throws SMPHookError {
        if (isRunning()) {
            throw SMPHookError.with(ErrorType.IO, "Server process is already running.");
        }

        String initCommand = getInitCommand();
        if (nogui) {
            initCommand = initCommand.concat(" nogui");
        }
        String[] commandTokens = initCommand.split("\\s+");

        ProcessBuilder processBuilder = new ProcessBuilder(commandTokens);
        File directory = serverJar.getParentFile();
        if (directory != null) {
            processBuilder.directory(directory);
        }

        SMPHookError.throwIfFail(() -> {
            process = processBuilder.start();
            istream = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            ostream = new BufferedReader(new InputStreamReader(process.getInputStream()));

            process.onExit().thenAccept(p -> {
                process = null;
                istream = null;
                ostream = null;
            });
        });
    }

    /**
     * <p>Simulates the input you see in Minecraft: Java Edition, and processes the {@code command} as one complete command.
     *    Any escape characters are replaced with their literal equivalent (e.g. any {@code '\n'} are replaced as {@code '\\n'}).
     * </p>
     * 
     * @param command - the Minecraft command to be processed
     * @throws SMPHookError if the process is not running, or an {@link java.io.IOException} occurs
     */
    public void rawInput(String command) throws SMPHookError {
        if (!isRunning()) {
            throw SMPHookError.with(ErrorType.IO, "Server process is not running.");
        }

        SMPHookError.throwIfFail(() -> {
            istream.write(command);
            istream.newLine();
            istream.flush();
        });
    }

    /**
     * <p>Reads out the next line output by the server in a First In First Out (FIFO) order.</p>
     * 
     * <p>If the End Of Line (EOF) character ({@code "\0"}) is returned by this method, the server is no longer producing output.</p>
     * 
     * @return the next line from the server
     * @throws SMPHookError if the process is not running, or an {@link java.io.IOException}
     */
    public String rawOutput() throws SMPHookError {
        if (!isRunning()) {
            throw SMPHookError.with(ErrorType.IO, "Server process is not running.");
        }

        return SMPHookError.throwIfFail(() -> {
            String line = ostream.readLine();
            return line == null ? EOF : line;
        });
    }

    /**
     * <p>Checks to see if the server process in running.
     *    If it is, it will also check if the input and output streams are non-null.
     *    If not an {@link SMPHookError} is thrown.
     * </p>
     * 
     * @return {@code true} if running (+ in typical state), or {@code false} if otherwise
     * @throws SMPHookError if the process is in an unusual state
     */
    public boolean isRunning() throws SMPHookError {
        boolean active = process != null;
        if (active && (istream == null || ostream == null)) {
            throw SMPHookError.with(ErrorType.IO, "ServerProcess in unusual state - force stopping.");
        }
        return active;
    }

    /**
     * <p>Constructs the server initialisation command using its provided serverJar,
     *    minimum and maximum heap allocation arguments, and JVM options.
     * </p>
     * 
     * @return the formatted command to initialise the server
     */
    public String getInitCommand() {
        StringBuilder commandBuilder = new StringBuilder("java ");
        if (minHeap != null) commandBuilder.append(minHeap.toXms()).append(" ");
        if (maxHeap != null) commandBuilder.append(maxHeap.toXmx()).append(" ");

        for (JVMOption option : options) {
            commandBuilder.append(option).append(" ");
        }

        commandBuilder.append("-server -jar ").append(serverJar.getName());
        return commandBuilder.toString();
    }

    /** @return the server jar that this process will or is executing */
    public File getServerJar() {
        return serverJar;
    }

    /** @return the minimum heap allocation pool argument wrapped in an optional */
    public Optional<HeapArg> getMinHeap() {
        return Optional.ofNullable(minHeap);
    }

    /** @return the maximum heap allocation pool argument wrapped in an optional. */
    public Optional<HeapArg> getMaxHeap() {
        return Optional.ofNullable(maxHeap);
    }

    /** @return the experimental Java Virtual Machine (JVM) options configured for this server process */
    public JVMOption[] getOptions() {
        return options;
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(serverJar, minHeap, maxHeap, options);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof ServerProcess asProc)) return false;

        final BiFunction<HeapArg, HeapArg, Boolean> check = (arg1, arg2) -> {
            if (arg1 == null && arg2 == null)       return true;
            if (arg1 != null && !arg1.equals(arg2)) return false;
            return true;
        };

        if (!check.apply(minHeap, asProc.minHeap) || !check.apply(maxHeap, asProc.maxHeap)) {
            return false;
        }

        return serverJar.equals(asProc.serverJar) && Arrays.equals(options, asProc.options);
    }

    @Override
    public String toString() {
        return String.format("ServerProcess[-M:%s, +M:%d, O:%s]", minHeap.toXms(), maxHeap.toXmx(), Arrays.toString(options));
    }
}
