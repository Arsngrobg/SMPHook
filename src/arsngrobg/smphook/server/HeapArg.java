package arsngrobg.smphook.server;

import java.util.Objects;

import arsngrobg.smphook.annotations.NonNull;

/**
 * <h1>JVM Heap Argument</h1>
 * <p>The {@code HeapArg} class represents a JVM heap allocation argument, with a given {@code size} and {@link Unit}.
 *    The {@code size} is a positive, non-zero integer value, and the {@code unit} specifies the amount of memory in different units ({@link Unit#BYTE}, {@link Unit#KILOBYTE}, {@link Unit#MEGABYTE}, {@link Unit#GIGABYTE}).
 * </p>
 * <p>A {@code HeapArg} object is comparable with another {@code HeapArg} object through the {@link #compareTo(HeapArg)} method, or through the static {@link HeapArg#compareTo(HeapArg, HeapArg)} method.</p>
 * <blockqoute><pre>
 * HeapArg arg1 = new HeapArg(20000);
 * HeapArg arg2 = new HeapArg(20, HeapArg.Unit.KILOBYTE);
 * System.out.println(arg1.compareTo(arg2)); // output: 0
 * </pre></blockqoute>
 * 
 * @since  1.0
 * @author Arsngrobg
 */
public final class HeapArg implements Comparable<HeapArg> {
    /**
     * Compares the two heap arguments: {@code arg1} & {@code arg2} and returns the relative difference between the two arguments.
     * @param arg1 - the first argument to compare to the second argument
     * @param arg2 - the second argument to compare to the first argument
     * @return the relative difference between {@code arg1} & {@code arg2}
     */
    public static int compare(@NonNull HeapArg arg1, @NonNull HeapArg arg2) {
        int difference = arg1.unit.compareTo(arg2.unit);
        long s1 = arg1.size * (long) Math.pow(1000, Math.max(0,  difference));
        long s2 = arg2.size * (long) Math.pow(1000, Math.max(0, -difference));
        return Long.compare(s1, s2);
    }

    /**
     * <p>Formats the given {@code arg} as a minimum bound argument for the JVM.</p>
     * <p>Example: {@code HeapArg(2, Unit.GIGABYTE)} gets formatted to {@code "-Xms2G"}</p>
     * @param arg - a nullable {@code HeapArg}
     * @return the formatted heap argument, or an empty string ({@code ""}) if {@code arg} is {@code null}
     */
    public static String asMinJVM(HeapArg arg) {
        if (arg == null) return "";
        return String.format("-Xms%s", arg);
    }

    /**
     * <p>Formats the given {@code arg} as a maxmimum bound argument for the JVM.</p>
     * <p>Example: {@code HeapArg(2, Unit.GIGABYTE)} gets formatted to {@code "-Xmx2G"}</p>
     * @param arg - a nullable {@code HeapArg}
     * @return the formatted heap argument, or an empty string ({@code ""}) if {@code arg} is {@code null}
     */
    public static String asMaxJVM(HeapArg arg) {
        if (arg == null) return "";
        return String.format("-Xmx%s", arg);
    }

    /**
     * <h1>JVM Heap Argument Unit</h1>
     * <p>Defines the common units used for representing JVM heap size arguments.
     *    It supports the four main units supported across all JVMs: {@code BYTE}, {@code KILOBYTE}, {@code MEGABYTE}, and {@code GIGABYTE}, each with a representative {@code suffix} character.
     * </p>
     * <p>This enum allows for human-readable representation through the {@link #toString()} method.</p>
     * 
     * @since  1.0
     * @author Arsngrobg
     */
    public static enum Unit {
        BYTE    ('B'),
        KILOBYTE('K'),
        MEGABYTE('M'),
        GIGABYTE('G');

        /** The suffix of this unit enum. */
        public final char suffix;

        private Unit(char suffix) {
            this.suffix = suffix;
        }

        @Override
        public String toString() {
            return String.format("%c%s", Character.toUpperCase(name().charAt(0)), name().substring(1));
        }
    }

    private final long size;
    private final Unit unit;

    /**
     * <p>Initialises a JVM heap argument with the given {@code size} & {@code unit}.</p>
     * <p>The {@code size} must be a positive, non-zero integer value, denoting the relative memory size in the given {@code unit}.</p>
     * @param size - a positive, non-zero integer value
     * @param unit - enum value representing the scale of the {@code size}
     * @throws Error if the {@code size} is invalid or the {@code unit} is {@code null}
     */
    public HeapArg(long size, @NonNull Unit unit) throws Error {
        if (size <= 0)    throw new Error("SMPHookError: HeapArg size cannot be less-than or equal to zero.");
        if (unit == null) throw new Error("SMPHookError: HeapArg unit cannot be null.");

        this.size = size;
        this.unit = unit;
    }

    /**
     * <p>Initialises a JVM heap argument with the given {@code size}, represented in bytes.</p>
     * <p>The {@code size} must be a positive, non-zero integer value, denoting the relative memory size in bytes.</p>
     * @param size - a positive, non-zero integer value
     * @throws Error if the {@code size} is invalid
     */
    public HeapArg(long size) throws Error {
        this(size, Unit.BYTE);
    }

    /** @return the memory size, relative to this instance's {@code unit} */
    public long getSize() {
        return size;
    }

    /** @return the enum representing the scale of the {@code size} */
    public Unit getUnit() {
        return unit;
    }

    @Override
    public int compareTo(HeapArg o) {
        return HeapArg.compare(this, o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        HeapArg asHeapArg = (HeapArg) obj;
        return compare(this, asHeapArg) == 0;
    }

    @Override
    public String toString() {
        return String.format("%d%s", size, unit.suffix);
    }
}
