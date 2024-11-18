package arsngrobg.smphook.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

/**
 * Wrapper for a {@link Process} that runs an instance of a Minecraft Java server.
 * On creation, you will be able to access the input and output buffers of the server,
 * and can read and write to them using the {@link #rawInput(String)} & {@link #rawOutput()} methods.
 */
public final class Server {
    private static final int    MAX_INPUT_CHARS = 256;
    private static final String FILE_EXTENSION = ".jar";
    private static final String INIT_COMMAND = "java %s %s -jar %s";

    private final String  jarName;
    private final String  directory;
    private final HeapArg minHeap;
    private final HeapArg maxHeap;

    private final Process process;
    private final BufferedWriter inputBuffer;
    private final BufferedReader outputBuffer;

    private boolean finished;

    /**
     * Initialises an instance of a Minecraft Java server. As long as the {@code jarPath} points to a valid JAR file,
     * and the {@code minHeap} & {@code maxHeap} arguments
     * @param jarPath - path which should point to a valid JAR file.
     * @param minHeap - the minimum amount of memory to allocate for the server's JVM (can be <i>nullable</i>).
     * @param maxHeap - the maximum amount of memory to allocate for the server's JVM (can be <i>nullable</i>).
     * @throws SMPHookError
     */
    public Server(String jarPath, HeapArg minHeap, HeapArg maxHeap) {
        if (minHeap != null && maxHeap != null) {
            int comparison = minHeap.compareTo(maxHeap);
            if (comparison == 1) throw new Error("Mismatched Heap Arguments.");
        }

        int seperationIndex = jarPath.lastIndexOf(File.separator);
        if (seperationIndex != -1) {
            directory = jarPath.substring(0, seperationIndex);
            jarName   = jarPath.substring(seperationIndex + 1, jarPath.length());
        } else {
            directory = null;
            jarName   = jarPath;
        }

        if (!jarName.endsWith(FILE_EXTENSION)) {
            throw new Error("The file that jarName points to is not a .jar file.");
        }

        this.maxHeap = maxHeap;
        this.minHeap = minHeap;

        String[] commandTokens = getInitCommand().split("\\s+");

        ProcessBuilder processBuilder = new ProcessBuilder(commandTokens);
        processBuilder.redirectErrorStream(true);
        if (directory != null) processBuilder.directory(new File(directory));

        try {
            process      = processBuilder.start();
            inputBuffer  = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            outputBuffer = new BufferedReader(new InputStreamReader(process.getInputStream()));

            process.onExit().thenAccept(p -> finished = true);
        } catch (IOException ignored) { throw new Error("Server Init Failure."); }
    }

    /**
     * Reads a single line of output from the Minecraft server process.
     * This method fetches one line at a time from the server's standard output stream.
     * It is blocking and will return {@code null} if an I/O error occurs or if no data is available.
     * @return the next line of server output, or {@code null} if an error occurs or no data is available.
     */
    public String rawOutput() {
        try {
            String line = outputBuffer.readLine();
            return line;
        } catch (IOException ignored) { return null; }
    }

    /**
     * Writes a command or input string to the Minecraft server process.
     * This method sends the specified input to the server's standard input stream, followed by a newline.
     * @param str the command or input string to send to the server process.
     * @return {@code true} if the input was successfully sent, {@code false} if an I/O error occurred.
     */
    public boolean rawInput(String str) {
        if (str == null || str.length() >= MAX_INPUT_CHARS || str.length() == 0) {
            return false;
        }

        try {
            inputBuffer.write(str);
            inputBuffer.newLine();
            inputBuffer.flush();
            return true;
        } catch (IOException ignored) { return false; }
    }

    /**
     * Attempts to push a {@code stop} command to the server's input buffer.
     * If this fails, it will forcefully kill the server process.
     */
    public void stop() {
        boolean success = rawInput("stop");
        if (!success) process.destroy();
    }

    /** @return the command used to initialise the Minecraft server instance. */
    public String getInitCommand() {
        String minHeapStr = minHeap == null ? "" : minHeap.formatAsMin();
        String maxHeapStr = maxHeap == null ? "" : maxHeap.formatAsMax();
        String command = String.format(INIT_COMMAND, minHeapStr, maxHeapStr, jarName);
        return command;
    }

    /** @return whether the server instance is running. */
    public boolean isRunning() {
        return finished;
    }

    /** @return the name of the JAR file */
    public String getJarName() {
        return jarName;
    }

    /** @return the directory where the JAR file is located */
    public String getDirectory() {
        return directory;
    }

    /** @return the minimum allocated memory for the server's JVM */
    public HeapArg getMinHeap() {
        return minHeap;
    }

    /** @return the maximum allocated memory for the server's JVM */
    public HeapArg getMaxHeap() {
        return maxHeap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory, jarName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        Server asServer = (Server) obj;
        return hashCode() == asServer.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Server[init: %s]", getInitCommand());
    }
}
