package dev.arsngrobg.smphook.core;

import java.util.Arrays;

import dev.arsngrobg.smphook.SMPHookError;
import dev.arsngrobg.smphook.SMPHookError.Type;

/**
 * <p>The {@code HeapArg} class wraps a JVM heap allocation argument into a neat object wrapper.</p>
 * <p>It consists of a {@code size} and {@code unit} (defined by the {@link HeapArg.Unit} enum).</p>
 * <p>A {@code HeapArg} object can also be constructed from its helper method: {@link #fromString(String)}</p>
 * 
 * @see ServerProcess
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class HeapArg implements Comparable<HeapArg> {
    private static final String ALLNUM_REGEX  = "^\\d+$";
    private static final String HEAPARG_REGEX = "^\\d+[A-Z]$";

    /**
     * <p>Creates a {@code HeapArg} object through its {@code String} representation.</p>
     * <p>This method always returns a valid {@code HeapArg} object - if parsing errors occur, a {@link SMPHookError} is thrown.</p>
     * @param argStr - the string to be parsed as a {@code HeapArg} object
     * @return a {@code HeapArg} object with the {@code size} and {@code unit} parsed from the {@code argStr}
     * @throws SMPHookError if {@code argStr} is {@code null} or {@code argStr} is an invalid JVM heap argument string
     */
    public static HeapArg fromString(String argStr) throws SMPHookError {
        if (argStr == null) SMPHookError.throwNullPointer("argStr");

        if (argStr.matches(ALLNUM_REGEX)) {
            long size = Long.parseLong(argStr);
            return new HeapArg(size);
        } else if (argStr.matches(HEAPARG_REGEX)) {
            long size = Long.parseLong(argStr.substring(0, argStr.length() - 1));
            Unit unit = Arrays.asList(Unit.values())
                              .stream()
                              .filter(u -> u.name().charAt(0) == argStr.charAt(argStr.length() - 1))
                              .findFirst()
                              .orElseThrow(() -> SMPHookError.getErr(Type.INVALID_HEAPARG_REPR));
            return new HeapArg(size, unit);
        } else throw SMPHookError.getErr(Type.INVALID_HEAPARG_SIZE);
    }

    /**
     * <p>Compares two heap arguments together using their relative difference with the lowest unit between them.</p>
     * <p>For example, if {@code arg1} has the unit of {@link Unit#KILOBYTE} and {@code arg2} has the unit of {@link Unit#MEGABYTE},
     *    the method will compare based on the amount of kilobytes both arguments have.
     * </p>
     * @param arg1 - the first {@code HeapArg} object
     * @param arg2 - the seconds {@code HeapArg} object
     * @return {@code 0} if equal, {@code -1} if {@code arg2} is greater, {@code 1} if {@code arg1} is greater
     * @throws SMPHookError if {@code arg1} and/or {@code arg2} is {@code null}
     */
    public static int compare(HeapArg arg1, HeapArg arg2) throws SMPHookError {
        if (arg1 == null) SMPHookError.throwNullPointer("arg1");
        if (arg2 == null) SMPHookError.throwNullPointer("arg2");

        int difference = arg1.unit.compareTo(arg2.unit);
        long s1 = arg1.size * (long) Math.pow(1000, Math.max(0,  difference));
        long s2 = arg2.size * (long) Math.pow(1000, Math.max(0, -difference));
        return Long.compare(s1, s2);
    }

    /** <p>The available unit types allowed for this class.</p> */
    public static enum Unit { BYTE, KILOBYTE, MEGABYTE, GIGABYTE }

    private final long size;
    private final Unit unit;

    /**
     * <p>Initialises a {@code HeapArg} object with the desired {@code size} in {@code unit} bytes.</p>
     * @param size - unsigned, non-zero integer representing the amount of bytes in its {@code unit}
     * @param unit - an enum value which represents the magnitude of the {@code size}
     * @throws SMPHookError if {@code size} is less-than-or-equal-to {@code zero} or if {@code unit} is {@code null}
     */
    public HeapArg(long size, Unit unit) throws SMPHookError {
        if (size <= 0) throw SMPHookError.getErr(Type.INVALID_HEAPARG_SIZE);
        if (unit == null) SMPHookError.throwNullPointer("unit");

        this.size = size;
        this.unit = unit;
    }

    /**
     * <p>Initialises a {@code HeapArg} object with the desired {@code size} in bytes.</p>
     * @param bytes - unsigned, non-zero integer representing the amount of bytes
     * @throws SMPHookError if {@code bytes} is less-than-or-equal-to {@code zero}
     */
    public HeapArg(long bytes) throws SMPHookError {
        this(bytes, Unit.BYTE);
    }

    /**
     * Formats this {@code HeapArg} object into a minimum memory allocation flag
     * @return this {@code HeapArg} object prepended with the -Xms string
     */
    public String toXms() {
        return String.format("-Xms%s", this);
    }

    /**
     * Formats this {@code HeapArg} object into a maxmimum memory allocation flag
     * @return this {@code HeapArg} object prepended with the -Xmx string
     */
    public String toXmx() {
        return String.format("-Xmx%s", this);
    }

    /** @return the size of this heap argument */
    public long getSize() {
        return size;
    }

    /** @return the unit (scale) of this heap argument */
    public Unit getUnit() {
        return unit;
    }

    @Override
    public int compareTo(HeapArg o) {
        return compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        HeapArg asArg = (HeapArg) obj;
        return compareTo(asArg) == 0;
    }

    @Override
    public String toString() {
        return String.format("%d%c", size, unit.name().charAt(0));
    }
}
