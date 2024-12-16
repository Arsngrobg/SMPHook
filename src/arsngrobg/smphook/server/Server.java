package arsngrobg.smphook.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Optional;
import java.util.Properties;

import arsngrobg.smphook.annotations.NonNull;

public final class Server {
    private static final    int MAX_COMMAND_LENGTH = 256;
    private static final String FILE_EXTENSION     = ".jar";
    private static final String INIT_CMD_TEMPLATE  = "java %s %s -XX:+UseZGC -XX:+UseAdaptiveSizePolicy -XX:+UseThreadPriorities -jar %s";

    private final File jarfile;
    private final Optional<HeapArg> minHeap;
    private final Optional<HeapArg> maxHeap;

    private Process        process;
    private BufferedWriter istream;
    private BufferedReader ostream;

    public Server(@NonNull File jarfile, HeapArg minHeap, HeapArg maxHeap) throws Error {
        if (jarfile == null) {
            throw new Error("SMPHookError: jarfile cannot be null.");
        }

        if (!jarfile.getName().endsWith(FILE_EXTENSION)) {
            throw new Error("SMPHookError: jarfile must be a valid .jar file");
        }

        if ((minHeap != null && maxHeap != null) && minHeap.compareTo(maxHeap) == 1) {
            throw new Error("SMPHookError: mismatched minimum and maximum heap bounds.");
        }

        File directory = jarfile.getParentFile();
        File eulaFile = new File(String.format("%s%seula.txt", directory == null ? "" : directory, File.separator));
        
        Properties eulaProperties = new Properties();
        try (FileInputStream fistream = new FileInputStream(eulaFile)) {
            eulaProperties.load(fistream);
        } catch (IOException e) {
            throw (Error) new Error("SMPHookError: Unable to locate the eula.txt file. Run the server once to produce defualt eula.txt file.").initCause(e);
        }

        String value = eulaProperties.getProperty("eula");
        if (value == null || !value.equals("true")) {
            throw new Error("SMPHookError: You have not agreed to the official Minecraft EULA, you must agree before continuing.");
        }
        
        this.jarfile = jarfile;
        this.minHeap = Optional.ofNullable(minHeap);
        this.maxHeap = Optional.ofNullable(maxHeap);
    }

    public void init(boolean nogui) {
        if (isRunning()) return;

        String command = getInitCommand();
        if (nogui) command = command.concat(" nogui");
        String[] commandTokens = command.split("\\s+");

        ProcessBuilder processBuilder = new ProcessBuilder(commandTokens);

        File directory = jarfile.getParentFile();
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
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void stop() {
        boolean safeStop = rawInput("stop");
        if (!safeStop) {
            process.destroy();
        }
    }

    public boolean rawInput(String command) {
        if (!isRunning()) return false;

        command = command.replaceAll("[\n]+", "");

        if (command == null || command.isEmpty() || command.length() >= MAX_COMMAND_LENGTH) {
            return false;
        }

        try {
            istream.write(command);
            istream.newLine();
            istream.flush();
            return true;
        } catch (IOException ignored) { return false; }
    }

    public String rawOutput() {
        if (!isRunning()) return null;

        try {
            String line = ostream.readLine();
            return line;
        } catch (IOException ignored) { return null; }
    }

    public String getInitCommand() {
        return String.format(
            INIT_CMD_TEMPLATE,
            maxHeap.isPresent() ? maxHeap.get().asMaxOption() : "",
            minHeap.isPresent() ? minHeap.get().asMinOption() : "",
            jarfile.getName()
        ).replaceAll("\\s+", " ");
    }

    public boolean isRunning() throws Error {
        boolean running = process != null;
        if (running && (istream == null || ostream == null)) {
            throw new Error("SMPHookError: Server process is unusual state, forcefully exiting.");
        }
        return running;
    }

    public File getJarfile() {
        return jarfile;
    }

    public HeapArg getMinHeap() {
        if (!minHeap.isPresent()) return null;
        return minHeap.get();
    }

    public HeapArg getMaxHeap() {
        if (!maxHeap.isPresent()) return null;
        return maxHeap.get();
    }

    @Override
    public int hashCode() {
        return (int) process.pid();
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
        return String.format("Server[%d]", process.pid());
    }
}
