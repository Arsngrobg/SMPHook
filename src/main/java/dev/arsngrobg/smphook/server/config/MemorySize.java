package dev.arsngrobg.smphook.server.config;

import java.util.Objects;

/**
 * <p>The {@code MemorySize} class is a compressed shorthand for representing memory sizes.</p>
 * <p>These can be heap allocations or simple storage.
 *    Every {@code MemorySize} includes both a {@code size} and {@code unit} value; where the {@code unit} is the
 *    relative scaling factor and is what allows for this compressed interpretation and comparisons between other
 *    {@code MemorySize}s.
 * </p>
 * <p>This class provides <b>three</b> factories for wrapping raw values:
 *    <pre><code>
 *        var size = MemorySize.ofSize(20, Unit.MEGABYTE); // MemorySize[size: 20, unit: MEGABYTE]
 *        var size = MemorySize.ofBytes(20_971_520); // implicitly converts to MemorySize of "20M"
 *        var size = MemorySize.fromString("4G"); // MemorySize[size: 4, unit: GIGABYTE]
 *    </code></pre>
 * </p>
 * <p>As mentioned previously, the {@code MemorySize} value class can be compared with other {@code MemorySize} objects.
 *    <pre><code>
 *        var size1 = MemorySize.fromString("1025B");
 *        var size2 = MemorySize.fromString("1K"); // equivalent to "1024B"
 *        var cmp = size1.compareTo(size2);
 *        System.out.println(cmp); // outputs 1 - size1 is greater than size2
 *    </code></pre>
 * </p>
 *
 * @author  Arsngrobg
 * @since   v0.0.1-pre_alpha
 * @version v0.1
 * @see     MemorySize.Unit
 */
public final class MemorySize {
    /**
     * <p>This is the generic factory for creating a {@code MemorySize} object.</p>
     *
     * @param  size the relative 64-bit size for this
     * @param  unit the scaling component
     * @return      a new {@code MemorySize} object of the desired {@code size} and {@code unit}
     * @author      Arsngrobg
     * @since       v0.0.1-pre_alpha
     */
    public static MemorySize ofSize(final long size, final Unit unit) {
        if (size <= 0) throw new IllegalArgumentException("Size must be positive");
        if (unit == null) throw new NullPointerException("unit");
        return new MemorySize(size, unit);
    }

    /**
     * <p>The memory units supported by most JVMs.</p>
     * <p>It reaches a maximum of {@link Unit#EXABYTE} since that's the max addressing the 64-bit JVM can handle.</p>
     *
     * @author  Arsngrobg
     * @since   v0.0.1-pre_alpha
     * @version v1.0
     */
    public enum Unit {
        BYTE,
        KILOBYTE,
        MEGABYTE,
        GIGABYTE,
        TERABYTE,
        PETABYTE,
        EXABYTE
    }
    private final long size;
    private final Unit unit;

    public MemorySize(final long size, final Unit unit) {
        this.size = size;
        this.unit = unit;
    }

    /**
     * <p>Returns the 64-bit long value representing this memory size in the relative scale of its {@code unit}.</p>
     *
     * @return the size of this {@code MemorySize}
     * @author Arsngrobg
     * @since  v0.0.1-pre_alpha
     */
    public long getSize() {
        return size;
    }

    /**
     * <p>Returns the scaling component of this {@code MemorySize}.</p>
     * <p>This allows for the shorthand representation of extremely large byte sizes, including comparisons.</p>
     *
     * @return this {@code MemorySize}'s scaling component
     * @author Arsngrobg
     * @since  v0.0.1-pre_alpha
     */
    public Unit getUnit() {
        return unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, unit);
    }
}
