package dev.arsngrobg.smphook.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import static dev.arsngrobg.smphook.SMPHookError.condition;

/**
 * <p>The {@code ServerProcess} class wraps a {@link java.lang.Process}, running a Java Minecraft server process.</p>
 * 
 * <p>This class handles the stopping, starting, and I/O operations between SMPHook and the server.</p>
 * 
 * <p>A server process is highly customizable, allowing the use of Java Virtual Machine (JVM) options using the {@link JVMOption} class.
 *    The minimum and maximum allocation pools can be defined using the {@link HeapArg} class.
 *    This provides a near 1:1 interface for creating a server process but through the Java environment.
 * </p>
 * 
 * <p>The underlying process is not initiated on object instantiation, but rather through the {@link #init(boolean)} method.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    HeapArg
 * @see    JVMOption
 * @see    MinecraftServer
 */
public final class ServerProcess {
    /** <p>The String that represents the End Of File (EOF) character output by the server when it has finished running.</p> */
    public static final String EOF = "\0";

    // metadata
    private final File serverJar;
    private final Optional<HeapArg> minHeap;
    private final Optional<HeapArg> maxHeap;
    private final JVMOption[] options;

    // process data
    private Process process;
    private BufferedWriter istream;
    private BufferedReader ostream;

    /**
     * <p>Instantiates a {@code ServerProcess} object.</p>
     * 
     * <p>The constructor is for validation purposes, checking to see if the {@code serverJar} is the correct JAR file for Minecraft: Java Edition servers.</p>
     * 
     * <p>The {@code minHeap} & {@code maxHeap} arguments are <b>optional</b> and can be {@code null}.
     *    You can supply a variable number of Java Virtual Machine (JVM) options to customize the Java runtime (see {@link JVMOption}).
     *    These arguments affect the result of the method {@link #getInitCommand()}.
     * </p>
     * 
     * @param serverJar - the Minecraft: Java Edition server JAR file to be ran
     * @param minHeap - the minimum allocation pool for the server, can be {@code null}
     * @param maxHeap - the maximum allocation pool for the server, can be {@code null}
     * @param options - a variable number of JVM options
     * @throws SMPHookError if {@code serverJar} is: {@code null}, doesn't exist, or not a file; the {@code minHeap} & {@code maxHeap} are mismatched
     */
    public ServerProcess(String serverJar, HeapArg minHeap, HeapArg maxHeap, JVMOption... options) throws SMPHookError {
        this.serverJar = SMPHookError.throwIfFail(() -> new File(serverJar));
        SMPHookError.caseThrow(
            condition(() -> !this.serverJar.exists(), SMPHookError.with(ErrorType.FILE, "The serverJar provided does not exist.")),
            condition(() -> !this.serverJar.isFile(), SMPHookError.with(ErrorType.FILE, "The serverJar provided is not a file."))
        );

        this.minHeap = Optional.ofNullable(minHeap);
        this.maxHeap = Optional.ofNullable(maxHeap);
        // this is cleaner than using the Optional methods
        if ((minHeap != null && maxHeap != null) && minHeap.compareTo(maxHeap) == 1) {
            throw SMPHookError.withMessage("Mismatched HeapArgs.");
        }

        this.options = Stream.of(options).filter(o -> o != null).toArray(JVMOption[]::new);
    }

    /**
     * <p>Initialises the server process. If it is already running, an {@link SMPHookError} will be thrown.</p>
     * 
     * <p>The server process is initiated in the directory provided by the {@code serverJar} file.</p>
     * 
     * <p>I/O operations can be performed on this object with {@link #rawInput(String)} and {@link #rawOutput}.
     *    And the process can be terminated with either the {@link #stop()} or {@link #forceStop()}
     * </p>
     * 
     * @param nogui - whether to display the pre-packaged GUI on creation
     * @throws SMPHookError if the process cannot be started
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

        ProcessBuilder procBuilder = new ProcessBuilder(commandTokens);

        File directory = serverJar.getParentFile();
        if (directory != null) {
            procBuilder.directory(directory);
        }

        SMPHookError.throwIfFail(() -> {
            process = procBuilder.start();
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
     * @param command - the Minecraft command to process
     * @throws SMPHookError if the process is not running, or an {@link java.io.IOException} occurs
     */
    public void rawInput(String command) throws SMPHookError {
        if (!isRunning()) {
            throw SMPHookError.with(ErrorType.IO, "Server process is not running.");
        }

        String cleanedCommand = command.replaceAll("\n", "\\n");

        SMPHookError.throwIfFail(() -> {
            istream.write(cleanedCommand);
            istream.newLine();
            istream.flush();
        });
    }

    /**
     * <p>Reads out the next line that was output by the server in a First in First Out (FIFO) fashion.</p>
     *
     * <p>If the End Of File (EOF) character ({@code "\0"}) is returned by this method, the server is no longer prodoucing output (the server has finished running).</p>
     * 
     * @return the next line
     * @throws SMPHookError if the process is not running, or an {@link java.io.IOException} occurs
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
     * <p>Attemps to stop the server process by pumping the {@code stop} command to the process' input stream.</p>
     * 
     * <p>This method is not guaranteed to successfully stop the server (see {@link #forceStop()}).</p>
     * 
     * @throws SMPHookError if the process is not running, or an {@link java.io.IOException} occurs
     */
    public void stop() throws SMPHookError {
        rawInput("stop");
    }

    /**
     * <p>Forcefully stops the server process.</p>
     * 
     * @throws SMPHookError if the process is not running
     */
    public void forceStop() throws SMPHookError {
        if (!isRunning()) {
            throw SMPHookError.with(ErrorType.IO, "Server process is not running.");
        }
        process.destroyForcibly();
    }

    /**
     * <p>Checks to see if the server is running.
     *    If so, it checks to make sure the process is in a good state, if not an {@link SMPHookError} is thrown.
     *    The server process is also forcibly stopped.
     * </p>
     * 
     * @return {@code true} if the server process is running, {@code false} if otherwise
     * @throws SMPHookError if the server process is in an unusual state
     */
    public boolean isRunning() throws SMPHookError {
        boolean isRunning = process != null;
        if (isRunning && (istream == null || ostream == null)) { // this should never happen but always good to check
            forceStop();
            throw SMPHookError.with(ErrorType.IO, "Server process in unusual state - forcefully aborting.");
        }
        return isRunning;
    }

    /**
     * <p>Gets the PID of this server process.</p>
     * 
     * @return this server process' PID
     * @throws SMPHookError if the process is not running
     */
    public long getPID() throws SMPHookError {
        if (!isRunning()) {
            throw SMPHookError.with(ErrorType.IO, "Server process is not running.");
        }
        process.destroyForcibly();
        return process.pid();
    }

    /** @return the command that is used to initiate the server */
    public String getInitCommand() {
        StringBuilder commandBuilder = new StringBuilder("java ");
        minHeap.ifPresent(min -> commandBuilder.append(min.toXms()).append(" "));
        maxHeap.ifPresent(max -> commandBuilder.append(max.toXmx()).append(" "));

        for (JVMOption option : options) {
            commandBuilder.append(option).append(" ");
        }

        commandBuilder.append("-server -jar ").append(serverJar.getName());
        return commandBuilder.toString();
    }

    /** @return the server jar file that is running or to be running */
    public File getServerJar() {
        return serverJar;
    }

    /** @return the minimum allocation pool argument for this server process, can be {@code null} */
    public HeapArg getMinHeap() {
        return minHeap.orElse(null);
    }

    /** @return the maximum allocation pool argument for this server process, can be {@code null} */
    public HeapArg getMaxHeap() {
        return maxHeap.orElse(null);
    }

    /** @return the Java Virtual Machine (JVM) options used to customize this server process, can be <i>empty</i> */
    public JVMOption[] getOptions() {
        return options;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverJar, minHeap, maxHeap, options);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)                  return false;
        if (obj == this)                  return true;
        if (getClass() != obj.getClass()) return false;
        ServerProcess asProc = (ServerProcess) obj;
        
        // abort if min2 is null and min1 isnt
        // abort if min1 is null and min2 isnt
        // abort if min1 != min2

        // either null
        if (minHeap.isPresent() && asProc.minHeap.isEmpty() || minHeap.isEmpty() && asProc.minHeap.isPresent()) {
            return false;
        }

        // not equal
        else if (!minHeap.get().equals(asProc.minHeap.get())) {
            return false;
        }

        return serverJar.equals(asProc.serverJar) && Arrays.equals(options, asProc.options);
    }

    @Override
    public String toString() {
        if (!isRunning()) {
            return "ServerProcess[inactive]";
        }
        return String.format("ServerProcess[PID: %d]", getPID());
    }
}
