package arsngrobg.smphook.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import arsngrobg.smphook.SMPHookError;
import arsngrobg.smphook.server.heap.HeapArg;

public final class Server {
    private static final String FILE_EXTENSION = ".jar";
    private static final String INIT_COMMAND = "java %s %s -jar %s";

    private final String  jarName;
    private final String  directory;
    private final HeapArg minHeap;
    private final HeapArg maxHeap;

    private final Process process;
    private final BufferedWriter inputBuffer;
    private final BufferedReader outputBuffer;

    public Server(String jarPath, HeapArg minHeap, HeapArg maxHeap) throws SMPHookError {
        if (minHeap != null && maxHeap != null) {
            int comparison = minHeap.compareTo(maxHeap);
            if (comparison == 1) throw SMPHookError.get(SMPHookError.Type.MISMATCHED_HEAP_ARGS);
        }

        int seperationIndex = jarPath.lastIndexOf(File.separator);
        directory = jarPath.substring(0, seperationIndex);
        jarName   = jarPath.substring(seperationIndex + 1, jarPath.length());

        if (!jarName.endsWith(FILE_EXTENSION)) {
            throw SMPHookError.get(SMPHookError.Type.INVALID_FILE_EXTENSION);
        }

        this.maxHeap = maxHeap;
        this.minHeap = minHeap;

        String minHeapStr = minHeap == null ? "" : minHeap.formatAsMin();
        String maxHeapStr = maxHeap == null ? "" : maxHeap.formatAsMax();
        String[] commandTokens = String.format(INIT_COMMAND, minHeapStr, maxHeapStr, jarName).split("\\s+");

        ProcessBuilder processBuilder = new ProcessBuilder(commandTokens);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(new File(directory));

        try {
            process      = processBuilder.start();
            inputBuffer  = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            outputBuffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException ignored) { throw SMPHookError.get(SMPHookError.Type.SERVER_INIT_FAILURE); }
    }

    public String rawOutput() {
        try {
            String line = outputBuffer.readLine();
            return line;
        } catch (IOException ignored) { return null; }
    }

    public boolean rawInput(String str) {
        try {
            inputBuffer.write(str);
            inputBuffer.newLine();
            inputBuffer.flush();
            return true;
        } catch (IOException ignored) { return false; }
    }

    public String getJarName() {
        return jarName;
    }

    public String getDirectory() {
        return directory;
    }

    public HeapArg getMinHeap() {
        return minHeap;
    }

    public HeapArg getMaxHeap() {
        return maxHeap;
    }
}
