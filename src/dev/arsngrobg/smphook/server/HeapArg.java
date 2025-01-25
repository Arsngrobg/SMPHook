package dev.arsngrobg.smphook.server;

import java.util.Objects;
import java.util.stream.Stream;

import dev.arsngrobg.smphook.SMPHookError;
import static dev.arsngrobg.smphook.SMPHookError.condition;

/**
 * <p>The {@code HeapArg} object represents a Java Virtual Machine (JVM) heap allocation argument.</p>
 * 
 * <p>It consists of an unsigned {@code size} and a {@code Unit} (defined by the {@link Unit} enum).</p>
 * 
 * <p>It can be constructed by its two constructors or class method {@link HeapArg#fromString(String)}.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    ServerProcess
 */
public final class HeapArg implements Comparable<HeapArg> {
    private static final String ALLNUM_REGEX = "^\\d+$";
    private static final String PROPER_REGEX = "^\\d+[A-Za-z]$";

    /**
     * <p>Reverse engineers the supplied {@code argStr} string into its equivalent {@code HeapArg} object.</p>
     * 
     * @param argStr - the string to parse as a {@code HeapArg} object
     * @return a {@code HeapArg} object
     * @throws SMPHookError if the supplied {@code argStr} is {@code null} or does not match the expected pattern for a {@code HeapArg} string 
     */
    public static HeapArg fromString(String argStr) throws SMPHookError {
        if (argStr == null) throw SMPHookError.nullReference("argStr");

        if (argStr.matches(ALLNUM_REGEX)) {
            long bytes = Long.parseLong(argStr);
            return new HeapArg(bytes);
        } else if (argStr.matches(PROPER_REGEX)) {
            long size = Long.parseLong(argStr.substring(0, argStr.length() - 1));
            Unit unit = Stream.of(Unit.values())
                              .filter(u -> u.name().charAt(0) == argStr.charAt(argStr.length() - 1))
                              .findFirst()
                              .orElseThrow(() -> SMPHookError.withMessage("HeapArg unit suffix is invalid."));
            return new HeapArg(size, unit);
        } else throw SMPHookError.withMessage("argStr is not a proper HeapArg string.");
    }

    /** <p>The units supported by many of the JVM implementations.</p> */
    public static enum Unit {
        BYTE, KILOBYTE, MEGABYTE, GIGABYTE
    }

    private final long size;
    private final Unit unit;

    /**
     * <p>Instantiates a {@code HeapArg} object with the supplied {@code size} and {@code unit}.</p>
     * 
     * @param size - an unsigned integer denoting the size of this heap argument in [<i>unit</i>]bytes
     * @param unit - an enum value denoting the scale of this heap arg
     * @throws SMPHookError if {@code size} is zero or -ve, or if {@code unit} is {@code null}
     */
    public HeapArg(long size, Unit unit) throws SMPHookError {
        SMPHookError.caseThrow(
            condition(() -> size <= 0, SMPHookError.withMessage("size needs to be an unsigned, non-zero integer.")),
            condition(() -> unit == null, SMPHookError.nullReference("unit"))
        );

        this.size = size;
        this.unit = unit;
    }

    /**
     * <p>Instantiates a {@code HeapArg} object with the supplied {@code bytes}.</p>
     * 
     * <p>This is syntatically-equivalent to {@code HeapArg(bytes, Unit.BYTE)}</p>
     * 
     * @param bytes - an unsigned integer denoting the number of bytes this argument is
     * @throws SMPHookError if {@code bytes} is {@code 0} or -ve
     */
    public HeapArg(long bytes) throws SMPHookError {
        this(bytes, Unit.BYTE);
    }

    /**
     * <p>Formats this {@code HeapArg} object as a minimum allocation pool argument.</p>
     * 
     * @return this object's {@link #toString()} method prefixed with {@code -Xms}
     */
    public String toXms() {
        return String.format("-Xms%s", toString());
    }

    /**
     * <p>Formats this {@code HeapArg} object as a maximum allocation pool argument.</p>
     * 
     * @return this object's {@link #toString()} method prefixed with {@code -Xmx}
     */
    public String toXmx() {
        return String.format("-Xmx%s", toString());
    }

    /** @return the size of this heap argument */
    public long getSize() {
        return size;
    }

    /** @return the unit of the heap argument */
    public Unit getUnit() {
        return unit;
    }

    @Override
    public int compareTo(HeapArg o) {
        int unitDifference = unit.compareTo(o.unit);
        // if unit is ordinally greater than o.unit: diff is +ve
        // if unit is ordinally less than o.unit: diff is -ve
        long s1 =   size * (long) Math.pow(1000, Math.max(0, -unitDifference));
        long s2 = o.size * (long) Math.pow(1000, Math.max(0,  unitDifference));
        return Long.compare(s1, s2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)                  return false;
        if (obj == this)                  return true;
        if (getClass() != obj.getClass()) return false;
        HeapArg asArg = (HeapArg) obj;
        return compareTo(asArg) == 0;
    }

    @Override
    public String toString() {
        return String.format("%d%c", size, unit.name().charAt(0));
    }
}
