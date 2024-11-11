package arsngrobg.smphook.server;

import java.util.Objects;

/** Class which wraps a JVM heap argument. Holds a {@code size} and {@link HeapUnit}. */
public final class HeapArg implements Comparable<HeapArg> {
    /**
     * Parses the given {@code argStr} parameter as if it was a JVM heap argument (e.g. 3G or 112M).
     * @param argStr - the string to be parsed
     * @return a {@link HeapArg} object if the {@code argStr} parameter is a valid JVM argument
     */
    public static HeapArg fromString(String argStr) {
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
        } catch (NumberFormatException ignored) { throw new Error("Invalid heap argument size."); }
    }

    /** Set of values that describe units that the JVM recognise when processing heap arguements. */
    public static enum Unit {
        BYTE    ('B'),
        KILOBYTE('K'),
        MEGABYTE('M'),
        GIGABYTE('G');

        public char suffix;

        Unit(char suffix) {
            this.suffix = suffix;
        }
    };

    private final long size;
    private final Unit unit;

    /**
     * Initializes a JVM heap argument with the {@link HeapUnit} of {@link HeapUnit#BYTE}.
     * @param size - the size in bytes
     */
    public HeapArg(long size) {
        this(size, Unit.BYTE);
    }

    /**
     * Initialises a JVM heap argument with the {@code size} and {@code unit}.
     * @param size - the size in the relative {@code unit}
     * @param unit - the order of magnitude of the heap argument
     */
    public HeapArg(long size, Unit unit) {
        if (unit == null) throw new Error("nullptr");
        if (size <= 0)    throw new Error("Illegal Heap Argument Size.");

        this.size = size;
        this.unit = unit;
    }

    /** @return The heap argument formatted as if it was to be the minimum allocated memory for the heap in the JVM. */
    public String formatAsMin() {
        return String.format("-Xms%s", this);
    }

    /** @return The heap argument formatted as if it was to be the maximum allocated memory for the heap in the JVM. */
    public String formatAsMax() {
        return String.format("-Xmx%s", this);
    }

    /** @return The relative size of the JVM heap argument in its {@code unit} */
    public long getSize() {
        return size;
    }

    /** @return The order of magnitude of the JVM heap argument. */
    public Unit getUnit() {
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
    public int hashCode() {
        return Objects.hash(size, unit);
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
        return String.format("%d%c", size, unit.suffix);
    }
}
