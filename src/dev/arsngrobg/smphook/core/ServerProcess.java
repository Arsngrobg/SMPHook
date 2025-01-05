package dev.arsngrobg.smphook.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.Type;

/**
 * <p>This is an interface for a {@link java.lang.Process} for a Minecraft server instance.</p>
 * <p>This handles the I/O operations between the SMPHook and the server instance ({@link #rawInput(String)} & {@link #rawOutput()}).</p>
 * <p>The process is not initiated on creation, rather through the {@link #init(boolean)} method.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class ServerProcess {
    // based off what the Minecraft provides in the default installation
    private static final String INIT_CMD_TEMPLATE = "java %s %s -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M -jar %s";
    
    /** <p>The maximum allowed command length.</p> */
    public static final int MAX_COMMAND_LENGTH = 256;

    private final File jarFile;
    private final Optional<HeapArg> minHeap;
    private final Optional<HeapArg> maxHeap;

    private Process process;
    private BufferedWriter istream;
    private BufferedReader ostream;

    /**
     * <p>Prepares the server process by validating the {@code jarFile} and the heap arguments: {@code minHeap} & {@code maxHeap}.</p>
     * <p>If the {@code jarFile} is:
     *    <ul>
     *        <li>{@code null}</li>
     *        <li>doesn't exist</li>
     *        <li>not actually a jar file</li>
     *        <li>the manifest's <i>Main-Class</i> attribute does not point to a Minecraft main</li>
     *    </ul>
     *    then the {@code jarFile} is considered invalid and not accepted.
     * </p>
     * <p>The {@code minHeap} and {@code maxHeap} arguments are optional.
     *    If both are not {@code null}, then they will be compared to make sure the min arg is not greater than the max arg.</p>
     * @param jarFile - the Minecraft server jar to be wrapped
     * @param minHeap - the minimum allocation pool for the server - can be {@code null}
     * @param maxHeap - the maximum allocation pool for the server - can be {@code null}
     * @throws SMPHookError if {@code jarFile} is invalid and/or both min and heap args mismatched
     */
    public ServerProcess(String jarFile, HeapArg minHeap, HeapArg maxHeap) throws SMPHookError {
        if (jarFile == null) SMPHookError.throwNullPointer("jarFile");

        this.jarFile = new File(jarFile);
        if (!this.jarFile.getName().endsWith(".jar")) {
            throw SMPHookError.getErr(Type.SERVERPROC_NOT_JARFILE);
        }

        if (!this.jarFile.exists()) {
            throw SMPHookError.getErr(Type.SERVERPROC_JARFILE_NOEXIST);
        }

        try (JarFile asJarFile = new JarFile(this.jarFile)) {
            Manifest manifest = asJarFile.getManifest();
            String mainClass = manifest.getMainAttributes().getValue("Main-Class");
            if (!mainClass.equals("net.minecraft.bundler.Main")) {
                throw SMPHookError.getErr(Type.SERVERPROC_JARFILE_INVALID);
            }
        } catch (IOException e) { SMPHookError.throwWithCause(e); }

        this.minHeap = Optional.ofNullable(minHeap);
        this.maxHeap = Optional.ofNullable(maxHeap);
        this.minHeap.ifPresent(min -> {
            this.maxHeap.ifPresent(max -> {
                if (min.compareTo(max) > 0) {
                    throw SMPHookError.getErr(Type.SERVERPROC_MISMATCHED_HEAPARGS);
                }
            });
        });
    }

    /**
     * <p>Intialises the server process.</p>
     * <p>This starts the Minecraft server if it is not already running - otherwise this method does nothing.</p>
     * <p>The server process is initiated in the directory defined when creating this object (through the {@code jarFile} string).</p>
     * <p>I/O operation can be made with {@link #rawInput(String)} and {@link #rawOutput()},
     *    and the process can be terminated with either {@link #stop()} or {@link #forceStop()}
     * </p>
     * @param nogui - whether to display the GUI on creation
     * @throws SMPHookError if the process could not be started or if communication couldn't be made with the underlying process
     */
    public void init(boolean nogui) throws SMPHookError {
        if (isRunning()) return;

        String command = getInitCommand();
        if (nogui) command = command.concat(" nogui");
        String[] commandTokens = command.split("\\s");

        ProcessBuilder processBuilder = new ProcessBuilder(commandTokens);
        
        File directory = jarFile.getParentFile();
        if (directory != null) {
            processBuilder.directory(directory);
        }

        processBuilder.redirectErrorStream(true);

        try {
            process = processBuilder.start();
            istream = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            ostream = new BufferedReader(new InputStreamReader(process.getInputStream()));

            process.onExit().thenAccept(p -> {
                process = null;
                istream = null;
                ostream = null;
            });
        } catch (IOException e) {
            forceStop();
            SMPHookError.throwWithCause(e);
        }
    }

    /**
     * <p>Pushes the {@code command} into the server process' input stream.</p>
     * <p>This method does nothing if:
     *    <ul>
     *       <li>the server process is not running</li>
     *       <li>the {@code command} is {@code null}</li>
     *       <li>the {@code command} length is greater than {@value #MAX_COMMAND_LENGTH}</li>
     *    </ul>
     * </p>
     * <p>If an {@link java.io.IOException} occurs, then the method will also return {@code false}</p>
     * @param command - the literal {@code String} to be sent to the process
     * @return whether the push was successful or not
     */
    public boolean rawInput(String command) {
        if (!isRunning()) return false;

        if (command == null) return false;
        command = command.replaceAll("\n", "\\n");

        if (command.length() >= MAX_COMMAND_LENGTH) return false;

        try {
            istream.write(command);
            istream.newLine();
            istream.flush();
        } catch (IOException ignored) { return false; }

        return true;
    }

    /**
     * <p>Gets the next line from the server process' output stream.</p>
     * <b>This method is blocking and will return when a new line has been found.</b>
     * <p>An {@link Optional} is returned as this method may not always return a {@code String} and it may be {@code null}.
     *    However, in most situations it is a good indicator that the process has been terminated and the reader has reached the end of the output stream.
     * </p>
     * @return an {@link Optional} containing the next line
     */
    public Optional<String> rawOutput() {
        if (!isRunning()) return Optional.empty();

        try {
            String line = ostream.readLine();
            return Optional.ofNullable(line);
        } catch (IOException ignored) { return Optional.empty(); }
    }

    /** <p>Forcefully terminates the underlying process.</p> */
    public void forceStop() {
        process.destroyForcibly();
    }

    /**
     * <p>As opposed to {@link #forceStop()}, this method attempts to safely stop the server by pushing the {@code "stop"} command to the process.
     *    However, if it fails to do so, the method will rely on the {@link #forceStop()} method to kill the process.
     * </p>
     */
    public void stop() {
        boolean safeStop = rawInput("stop");
        if (!safeStop) forceStop();
    }

    /**
     * <p>Parses the given {@code jarFile}, and the min and max heap arguments if neccessary, into the initialisation command used to start the server.</p>
     * @return the formatted command string
     */
    public String getInitCommand() {
        return String.format(
            INIT_CMD_TEMPLATE,
            minHeap.map(min -> min.toXms()).orElse(""),
            maxHeap.map(max -> max.toXmx()).orElse(""),
            jarFile.getName()
        ).replaceAll("\\s+", " ");
    }

    /**
     * <p>Checks to see whether the process is running or not.
     *    It does this through {@code null} check of the process.
     * </p>
     * <p>Additionally, it also checks the I/O streams of the process,
     *    if the process is running but the I/O streams are invalid, then the process is in an unusual state and an {@link SMPHookError} is thrown.
     * </p>
     * @return whether the process is running
     * @throws SMPHookError if the process is considered to be in an unusual state
     */
    public boolean isRunning() throws SMPHookError {
        boolean running = process != null;
        if (running && (istream == null || ostream == null)) {
            throw SMPHookError.getErr(Type.SERVERPROC_UNUSUAL_STATE);
        }
        return running;
    }

    /** @return the location of the Minecraft server jar file */
    public File getJarFile() {
        return jarFile;
    }

    /**
     * <p>This method returns a {@link HeapArg} wrapped in an {@link Optional} type to make the explicit declaration that the arguments are not required but useful.</p>
     * @return the {@code minHeap} wrapped in an {@link Optional} type
     */
    public Optional<HeapArg> getMinHeap() {
        return minHeap;
    }

    /**
     * <p>This method returns a {@link HeapArg} wrapped in an {@link Optional} type to make the explicit declaration that the arguments are not required but useful.</p>
     * @return the {@code maxHeap} wrapped in an {@link Optional} type
     */
    public Optional<HeapArg> getMaxHeap() {
        return maxHeap;
    }

    @Override
    public int hashCode() {
        return (int) process.pid();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        ServerProcess asProc = (ServerProcess) obj;
        return hashCode() == asProc.hashCode();
    }

    @Override
    public String toString() {
        return String.format("ServerProcess[%d]", process.pid());
    }
}
