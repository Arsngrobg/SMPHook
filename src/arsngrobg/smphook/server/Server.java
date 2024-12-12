package arsngrobg.smphook.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Optional;

import arsngrobg.smphook.annotations.NonNull;

public final class Server {
    private static final    int MAX_COMMAND_LENGTH = 256;
    private static final String FILE_EXTENSION     = ".jar";
    private static final String INIT_CMD_TEMPLATE  = "java %s %s -XX:+UseZGC -XX:+UseAdaptiveSizePolicy -XX:+UseThreadPriorities -jar %s";

    private final File jarfile;
    private final Optional<HeapArg> minHeap;
    private final Optional<HeapArg> maxHeap;

    private Process process;
    private BufferedWriter istream;
    private BufferedReader  ostream;

    public Server(@NonNull File jarfile, HeapArg minHeap, HeapArg maxHeap) throws Error {
        if (jarfile == null) {
            throw new Error("SMPHookError: jarfile cannot be null.");
        }

        if (!jarfile.getName().endsWith(FILE_EXTENSION)) {
            throw new Error("SMPHookError: jarfile must be a valid .jar file");
        }

        this.jarfile = jarfile;
        this.minHeap = Optional.ofNullable(minHeap);
        this.maxHeap = Optional.ofNullable(maxHeap);
    }

    public void init(boolean nogui) {
        if (isRunning()) return;

        String command = getInitCommand();
        if (nogui) command.concat(" nogui");
        String[] commandTokens = command.split("\\s+");

        ProcessBuilder processBuilder = new ProcessBuilder(commandTokens);

        String absPath = jarfile.getAbsolutePath();
        String jarName = jarfile.getName();
        String dirPath = absPath.substring(0, absPath.length() - jarName.length() - 1);
        processBuilder.directory(new File(dirPath));
        
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

    public boolean rawInput(String command) {
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
        );
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
        return process.hashCode();
    }
}
