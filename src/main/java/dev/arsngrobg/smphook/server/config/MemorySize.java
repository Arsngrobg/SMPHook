package dev.arsngrobg.smphook.server.config;

import java.util.Objects;

/**
 * <p>The {@code MemorySize} class represents an amount of memory as a tangible value class.</p>
 * <p>This class exposes <b>three</b> factories to create new instances of {@code MemorySize}:
 *    <pre>var bytes = MemorySize.ofBytes(20); // Strongly-typed class version of "20B"</pre>
 *    <pre>var memSize = MemorySize.ofSize(20, Unit.KILOBYTE); // strongly-typed class version of "20K"</pre>
 *    <pre>var parsed = MemorySize.fromString("20B"); // parses string into MemorySize[size: 20, unit: BYTE]</pre>
 * </p>
 *
 * @author  Arsngrobg
 * @since   v0.0.1-pre_alpha
 * @version 0.1
 * @see     JVMOption
 */
public final class MemorySize {
    /**
     * <p>The memory units widely supported by most JVM implementations.</p>
     *
     * @author Arsngrobg
     * @since  v0.0.1-pre_alpha
     */
    @SuppressWarnings("unused")
    public enum Unit {
        BYTE,
        KILOBYTE,
        MEGABYTE,
        GIGABYTE,
        TERABYTE
    }

    private final long size;
    private final Unit unit;

    private MemorySize(long size, Unit unit) {
        this.size = size;
        this.unit = unit;
    }

    /**
     * <p>Returns the size component of this {@code MemorySize}.</p>
     *
     * @return this {@code MemorySize}'s size component
     * @since  v0.0.1-pre_alpha
     */
    public long getSize() {
        return size;
    }

    /**
     * <p>Returns the unit component of this {@code MemorySize}.</p>
     *
     * @return this {@code MemorySize}'s unit component
     * @since  v0.0.1-pre_alpha
     */
    public Unit getUnit() {
        return unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof MemorySize asMemSize)) return false;
        return size ==  asMemSize.getSize() && unit == asMemSize.getUnit();
    }

    @Override
    public String toString() {
        return String.format("%d%s", size, unit);
    }
}
