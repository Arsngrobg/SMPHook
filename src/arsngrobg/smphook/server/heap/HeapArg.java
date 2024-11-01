package arsngrobg.smphook.server.heap;

import arsngrobg.smphook.SMPHookError;

/** Class which wraps a JVM heap argument. Holds a {@code size} and {@link HeapUnit}. */
public final class HeapArg implements Comparable<HeapArg> {
    private final long     size;
    private final HeapUnit unit;

    /**
     * Initializes a JVM heap argument with the {@link HeapUnit} of {@link HeapUnit#BYTE}.
     * @param size - the size in bytes
     */
    public HeapArg(long size) {
        this(size, HeapUnit.BYTE);
    }

    /**
     * Initialises a JVM heap argument with the {@code size} and {@code unit}.
     * @param size - the size in the relative {@code unit}
     * @param unit - the order of magnitude of the heap argument
     */
    public HeapArg(long size, HeapUnit unit) {
        if (unit == null) throw SMPHookError.get(SMPHookError.Type.NULL_POINTER);
        if (size <= 0)    throw SMPHookError.get(SMPHookError.Type.ILLEGAL_HEAP_ARGUMENT_SIZE);

        this.size = size;
        this.unit = unit;
    }

    /** @return The relative size of the JVM heap argument in its {@code unit} */
    public long getSize() {
        return size;
    }

    /** @return The order of magnitude of the JVM heap argument. */
    public HeapUnit getUnit() {
        return unit;
    }

    @Override
    public int compareTo(HeapArg o) {
        int powerDifference = Math.abs(unit.ordinal() - o.unit.ordinal());
        if (powerDifference < 0) { // o unit is greater
            long s1 =   size / (long) Math.pow(1000, powerDifference);
            long s2 = o.size;
            return Long.compare(s1, s2);
        } else if (powerDifference > 0) { // this unit is greater
            long s1 =   size;
            long s2 = o.size / (long) Math.pow(1000, powerDifference);
            return Long.compare(s1, s2);
        } else return Long.compare(size, o.size);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this)                                 return true;
        HeapArg asHeapArg = (HeapArg) obj;
        return compareTo(asHeapArg) == 0;
    }

    @Override
    public String toString() {
        return String.format("%d%c", size, unit.getSuffix());
    }
}
