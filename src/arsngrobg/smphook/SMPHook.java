package arsngrobg.smphook;

import arsngrobg.smphook.SMPHookError.Type;
import arsngrobg.smphook.server.heap.HeapArg;
import arsngrobg.smphook.server.heap.HeapUnit;

public final class SMPHook {
    private SMPHook() { throw new UnsupportedOperationException("SMPHook is a utility class."); }

    /**
     * Takes in the {@code argStr} parameter and parse it as if it was a JVM heap argument (e.g. 3G or 112M).
     * The HeapArg object is never {@code null}, if the {@code argStr} is invalid, the program will throw an error.
     * @param argStr - the string that is to be parsed
     * @return a {@link HeapArg} object if the {@code argStr} parameter is a valid JVM heap argument
     */
    public static HeapArg heapArg(String argStr) {
        if (argStr == null) SMPHookError.get(Type.NULL_POINTER);

        char lastChar = argStr.charAt(argStr.length() - 1);
        HeapUnit unit = null;
        for (HeapUnit u : HeapUnit.values()) {
            unit = u.getSuffix() == lastChar ? u : null;
        }

        if (unit == null) throw SMPHookError.get(SMPHookError.Type.INVALID_HEAP_ARGUMENT_FORMAT);

        String sizePortion = argStr.substring(0, argStr.length() - 1);
        long size = Long.parseLong(sizePortion);

        return new HeapArg(size, unit);
    }

    public static void main(String[] args) {
        System.out.println(heapArg("1G"));
    }
}
