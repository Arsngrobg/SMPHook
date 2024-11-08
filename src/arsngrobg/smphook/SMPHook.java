package arsngrobg.smphook;

import arsngrobg.smphook.server.Server;
import arsngrobg.smphook.server.types.HeapArg;
import arsngrobg.smphook.server.types.HeapArg.Unit;

/** Utility class for ease-of-use functions for the SMPHook. */
public final class SMPHook {
    private static Server instance;

    private SMPHook() { throw new UnsupportedOperationException("SMPHook is a utility class."); }

    /**
     * Takes in the {@code argStr} parameter and parse it as if it was a JVM heap argument (e.g. 3G or 112M).
     * The HeapArg object returned is never {@code null}, if the {@code argStr} is invalid, the program will throw an error.
     * @param argStr - the string that is to be parsed
     * @return a {@link HeapArg} object if the {@code argStr} parameter is a valid JVM heap argument
     */
    public static HeapArg heapArg(String argStr) {
        if (argStr == null) throw new Error("nullptr");

        char lastChar = argStr.charAt(argStr.length() - 1);
        Unit unit = null;
        for (Unit u : Unit.values()) {
            unit = u.suffix == lastChar ? u : null;
        }

        if (unit == null) throw new Error("Invalid unit suffix.");

        try {
            String sizePortion = argStr.substring(0, argStr.length() - 1);
            long size = Long.parseLong(sizePortion);
    
            return new HeapArg(size, unit);
        } catch (NumberFormatException ignored) { throw new Error(""); }
    }

    /**
     * Initialises a server instance.
     * @param jarPath - the path to the server jar file
     * @param minHeap - the minimum amount of memory to allocate to the JVM
     * @param maxHeap - the maximum amount of memory to allocate to the JVM
     * @return a {@link Server} instance
     */
    public static Server server(String jarPath, HeapArg minHeap, HeapArg maxHeap) {
        if (SMPHook.instance != null) {
            throw new Error("Server instance already available.");
        }
        SMPHook.instance = new Server(jarPath, minHeap, maxHeap);
        return SMPHook.instance;
    }

    /**
     * Gets the currently active instance of the Minecraft server.
     * If no such exists, then it will throw an error.
     * @return the active {@link Server} instance
     */
    public static Server server() {
        if (SMPHook.instance == null) {
            throw new Error("No server instance currently available.");
        }
        return SMPHook.instance;
    }

    public static void main(String[] args) throws Exception {
        server("smp\\server.jar", heapArg("2G"), heapArg("16G"));

        String line;
        while ( (line = server().rawOutput()) != null ) {
            System.out.println(line);
            server().rawInput("/");
        }
    }
}
