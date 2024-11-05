package arsngrobg.smphook;

import arsngrobg.smphook.SMPHookError.Type;
import arsngrobg.smphook.server.Server;
import arsngrobg.smphook.server.heap.HeapArg;
import arsngrobg.smphook.server.heap.HeapUnit;

/** Utility class for ease-of-use functions for the SMPHook. */
public final class SMPHook {
    private SMPHook() { throw new UnsupportedOperationException("SMPHook is a utility class."); }

    /**
     * Takes in the {@code argStr} parameter and parse it as if it was a JVM heap argument (e.g. 3G or 112M).
     * The HeapArg object returned is never {@code null}, if the {@code argStr} is invalid, the program will throw an error.
     * @param argStr - the string that is to be parsed
     * @return a {@link HeapArg} object if the {@code argStr} parameter is a valid JVM heap argument
     */
    public static HeapArg heapArg(String argStr) throws SMPHookError {
        if (argStr == null) throw SMPHookError.get(Type.NULL_POINTER);

        char lastChar = argStr.charAt(argStr.length() - 1);
        HeapUnit unit = null;
        for (HeapUnit u : HeapUnit.values()) {
            unit = u.getSuffix() == lastChar ? u : null;
        }

        if (unit == null) throw SMPHookError.get(SMPHookError.Type.INVALID_HEAP_ARGUMENT_FORMAT);

        try {
            String sizePortion = argStr.substring(0, argStr.length() - 1);
            long size = Long.parseLong(sizePortion);
    
            return new HeapArg(size, unit);
        } catch (NumberFormatException ignored) { throw SMPHookError.get(SMPHookError.Type.INVALID_HEAP_ARGUMENT_FORMAT); }
    }

    public static Server server(String jarPath, HeapArg minHeap, HeapArg maxHeap) throws SMPHookError {
        return new Server(jarPath, minHeap, maxHeap);
    }

    public static void main(String[] args) throws Exception {
        Server server = server("smp\\server.jar", heapArg("2G"), heapArg("16G"));
        String line;

        final int MAX_LEN = 40;
        int current = 0;
        int direction = 1;

        while((line = server.rawOutput()) != null) {
            System.out.println(line);

            Thread.sleep(50);

            server.rawInput(String.format("/say *%s", "*************************************************************".substring(0, MAX_LEN - current)));
            current += direction;

            if (current == MAX_LEN) direction = -1;
            if (current == 0)       direction =  1;
        }
    }
}
