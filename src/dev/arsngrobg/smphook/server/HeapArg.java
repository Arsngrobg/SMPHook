package dev.arsngrobg.smphook.server;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;
import static dev.arsngrobg.smphook.SMPHookError.condition;
import static dev.arsngrobg.smphook.SMPHookError.nullCondition;

/**
 * <p>The {@code HeapArg} class represents a Java Virtual Machine (JVM) heap allocation argument.
 *    It consists of an unsigned, non-zero {@code size} and a {@code unit} (defined by {@link HeapArg.Unit} enum).
 * </p>
 * 
 * <p>To construct a {@code HeapArg} object you can use the provided factory methods:
 *    <ul>
 *       <li>{@link #ofSize(long, Unit)} - creates a {@code HeapArg} object with the sepcified size and {@link HeapArg.Unit}</li>
 *       <li>{@link #ofBytes(long)} - creates a {@code HeapArg} object from a given number of bytes</li>
 *       <li>{@link #fromString(String)} - constructs a {@code HeapArg} object from its string representation</li>
 *    </ul>
 * </p>
 * 
 * <p>This class is immutable and implements the {@link java.lang.Comparable} interface,
 *    allowing relative comparisons between other {@code HeapArg} objects.
 * </p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    HeapArg.Unit
 * @see    #ofSize(long, Unit)
 * @see    #ofBytes(long)
 * @see    #fromString(String)
 * @see    ServerProcess
 */
public final class HeapArg implements Comparable<HeapArg> {
    /**
     * <p>Parses the supplied {@code argStr} into its equivalent {@code HeapArg} object.</p>
     * 
     * <p>The {@code argStr} can be a string consisting of only numbers (e.g. {@code 123} or a number followed by a unit suffix ({@code 123G}).</p>
     * 
     * @param argStr - the string to parse as a {@code HeapArg} object
     * @return a {@code HeapArg} object
     * @throws SMPHookError if the {@code argStr} is {@code null} or invalid
     * @see #ofSizeAndUnit(long, Unit)
     * @see #ofBytes(long)
     */
    public static HeapArg fromString(String argStr) throws SMPHookError {
        SMPHookError.caseThrow(
            nullCondition(argStr, "argStr"),
            condition(() -> argStr.length() == 0, SMPHookError.withMessage("'argStr' must be a string of length greater than zero."))
        );

        boolean isAllNum = true;

        for (int idx = 0; idx < argStr.length(); idx++) {
            char ch = argStr.charAt(idx);

            if (!Character.isLetterOrDigit(ch)) {
                throw SMPHookError.withMessage("argStr is an invalid JVM heap allocation argument string.");
            }

            if (Character.isAlphabetic(ch)) {
                if (idx == argStr.length() - 1) {
                    isAllNum = false;
                } else throw SMPHookError.withMessage("argStr is an invalid JVM heap allocation argument string.");
            }
        }

        if (isAllNum) {
            long bytes = Long.parseLong(argStr);
            return HeapArg.ofBytes(bytes);
        }

        long size = Long.parseLong(argStr.substring(0, argStr.length() - 1));
        char unitChar = argStr.charAt(argStr.length() - 1);
        
        Unit unit = null;
        for (Unit u : Unit.values()) {
            if (u.name().charAt(0) != unitChar) continue;
            unit = u;
        }

        return HeapArg.ofSize(size, unit);
    }

    /**
     * <p>Instantiates a {@code HeapArg} instance with the supplied amount of {@code bytes}.</p>
     * 
     * <p>This is syntatically-equivalent to {@code HeapArg.ofSize(bytes, Unit.BYTE)}</p>
     * 
     * @param bytes - an unsigned, non-zero integer denoting the amount of bytes this {@code HeapArg} is
     * @return a new {@code HeapArg} instance
     * @throws SMPHookError if {@code bytes} is -ve or zero
     */
    public static HeapArg ofBytes(long bytes) throws SMPHookError {
        return HeapArg.ofSize(bytes, Unit.BYTE);
    }

    /**
     * <p>Instantiates a {@code HeapArg} instance with the supplied {@code size} and {@code unit}.</p>
     * 
     * @param size - an unsigned, non-zero integer denoting the size of this heap argument
     * @param unit - an enum value denoting the scale of this heap arg
     * @return a new {@code heapArg} instance
     * @throws SMPHookError if {@code size} is -ve or zero, or {@code unit} is {@code null}
     */
    public static HeapArg ofSize(long size, Unit unit) throws SMPHookError {
        SMPHookError.caseThrow(
            condition(() -> size <= 0, SMPHookError.withMessage("Size needs to be a positive integer.")),
            nullCondition(unit, "unit")
        );

        return new HeapArg(size, unit);
    }

    /** <p>More descriptive constants for use when comparing two {@code HeapArg} objects.</p> 
     * 
     * @see #compareTo(HeapArg)
     */
    public static final int
        EQUALS       =  0,
        GREATER_THAN =  1,
        LESS_THAN    = -1;

    /** <p>The units supported by most of the JVM implementations. </p> */
    public enum Unit {
        BYTE, KILOBYTE, MEGABYTE, GIGABYTE
    }

    private final long size;
    private final Unit unit;

    private HeapArg(long size, Unit unit) {
        this.size = size;
        this.unit = unit;
    }

    /**
     * <p>Formats this {@code HeapArg} as a minimum allocation pool argument.</p>
     * 
     * @return this {@code HeapArg}'s string representation prefixed with {@code "-Xms"}
     */
    public String toXms() {
        return String.format("-Xms%s", toString());
    }

    /**
     * <p>Formats this {@code HeapArg} as a maximum allocation pool argument.</p>
     * 
     * @return this {@code HeapArg}'s string representation prefixed with {@code "-Xmx"}
     */
    public String toXmx() {
        return String.format("-Xmx%s", toString());
    }

    /** @return the size of this heap argument */
    public long getSize() {
        return size;
    }
    
    /** @return the unit of this heap argument */
    public Unit getUnit() {
        return unit;
    }

    @Override
    public int compareTo(HeapArg o) {
        SMPHookError.strictlyRequireNonNull(o, "o");

        int unitDifference = unit.compareTo(o.unit);
        // if unit has ordinal value greater than o.unit: diff is +ve
        // if unit has ordinal value less    than o.unit: diff is -ve
        long s1 =   size * (long) Math.pow(1000, Math.max(0, -unitDifference));
        long s2 = o.size * (long) Math.pow(1000, Math.max(0,  unitDifference));
        return Long.compare(s1, s2);
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(unit, size);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;
        HeapArg asArg = (HeapArg) obj;
        return compareTo(asArg) == 0;
    }

    @Override
    public String toString() {
        return String.format("%d%c", getSize(), getUnit().name().charAt(0));
    }
}
