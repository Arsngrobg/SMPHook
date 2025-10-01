package dev.arsngrobg.smphook.server.config;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
 *        var size = MemorySize.ofBytes(20_971_520); // implicitly converts to MemorySize of 20M
 *        var size = MemorySize.fromString("4G"); // MemorySize[size: 4, unit: GIGABYTE]
 *    </code></pre>
 * </p>
 * <p>As mentioned previously, the {@code MemorySize} value class can be compared with other {@code MemorySize} objects.
 *    <pre><code>
 *        var size1 = MemorySize.fromString("1025B");
 *        var size2 = MemorySize.fromString("1K"); // equivalent to 1024B
 *        var cmp = size1.compareTo(size2);
 *        System.out.println(cmp); // outputs 1 - size1 is greater than size2
 *    </code></pre>
 *    It performs relative comparisons using both {@code MemorySize}'s {@link Unit}s.
 * </p>
 *
 * @author  Arsngrobg
 * @since   v0.0.1-pre_alpha
 * @version v1.2
 * @see     MemorySize.Unit
 * @see     MemorySize#ofSize(long, Unit)
 * @see     MemorySize#ofBytes(long)
 * @see     MemorySize#fromString(String) 
 */
@SuppressWarnings("ClassCanBeRecord")
public final class MemorySize implements Comparable<MemorySize> {
    /** <p>This is the number of bytes per (binary) kilobyte.</p> */
    public static final int BYTES_PER_KILOBYTE = 1024;

    /**
     * <p>This is the common factory for defining a {@code MemorySize} value.</p>
     * <p>It parses the provided {@code memStr} into a tangible {@code MemorySize} object.
     *    <pre><code>
     *        var size = MemorySize.fromString("20"); // infers that you want to represent 20 bytes
     *        System.out.println(size); // output: 20B
     *        System.out.println(size.equals(MemorySize.fromString("20B")); // output: true
     *    </code></pre>
     *    As you can see, this method will infer that a raw integer value means you require this {@code MemorySize} to
     *    represent 20 bytes of memory/data.
     * </p>
     *
     * @param  memStr the string representation to reverse engineer the {@code MemorySize}
     * @return        a new {@code MemorySize} object, represented by the supplied {@code memStr}
     * @author        Arsngrobg
     * @since         v0.0.1-pre_alpha
     * @see           MemorySize#ofSize(long, Unit)
     * @see           MemorySize#ofBytes(long)
     */
    public static MemorySize fromString(final String memStr) {
        if (Objects.isNull(memStr)) throw new NullPointerException("memStr");
        if (memStr.isBlank()) throw new IllegalArgumentException("memStr cannot be blank");

        final char lastChar = memStr.charAt(memStr.length() - 1);
        final Optional<Unit> maybeUnit = Stream.of(Unit.values())
                                               .filter(u -> u.name().charAt(0) == lastChar)
                                               .findFirst();

        if (maybeUnit.isEmpty() && !Character.isDigit(lastChar)) {
            throw new IllegalArgumentException("memStr contains illegal unit suffix.");
        }

        final int subStrEnd = maybeUnit.isPresent() ? memStr.length() - 1 : memStr.length();
        final long size = Integer.parseInt(memStr.substring(0, subStrEnd));

        return maybeUnit.map(u -> MemorySize.ofSize(size, u))
                        .orElse(MemorySize.ofBytes(size));
    }

    /**
     * <p>This is a factory for creating a {@code MemorySize} from a number of {@code bytes}.</p>
     * <p>
     *    <pre><code>
     *        var bytes = MemorySize.ofBytes(1024);
     *        System.out.println(bytes); // output: 1024B
     *    </code></pre>
     * </p>
     *
     * @param  bytes the number of whole bytes that this {@code MemorySize} will carry
     * @return       a new {@code MemorySize} object, consisting of the number of provided {@code bytes}
     * @author       Arsngrobg
     * @since        v0.0.1-pre_alpha
     */
    public static MemorySize ofBytes(final long bytes) {
        return MemorySize.ofSize(bytes, Unit.BYTE);
    }

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
     * <p>The memory units supported by most JVMs, and some extras that could <i>theoretically</i> be supported by the
     *    JVM.
     * </p>
     * <p>It reaches a maximum of {@link Unit#EXABYTE} since that's the max scaler unit the 64-bit JVM can handle.</p>
     * <p>The ordinal value determines the difference in scale between each other unit scalar.</p>
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
     * <p>Converts this {@code MemorySize} into another {@code MemorySize} of the supplied {@code unit}.</p>
     * <p>The {@code unit} must be ordinally-lower than this {@code MemorySize}'s {@link Unit}.
     *    <pre><code>
     *        var size = MemorySize.fromString("1K");
     *        var bytes = size.toUnit(Unit.BYTE);
     *        System.out.printf("%s == %s\n", size, bytes); // output: 1K == 1024B
     *    </code></pre>
     *    If the ordinal value of it is equal, then it will just return the same value
     * </p>
     *
     * @param  unit the unit to convert this {@code MemorySize} into
     * @return      a new {@code MemorySize} of the desired unit scaling factor
     * @author      Arsngrobg
     * @since       v0.0.1-pre_alpha
     */
    public MemorySize toUnit(Unit unit) {
        if (this.unit.ordinal() < unit.ordinal()) {
            throw new IllegalStateException("Cannot convert MemorySize to a higher unit.");
        }

        return MemorySize.ofSize(
                this.size * (MemorySize.BYTES_PER_KILOBYTE * (this.unit.ordinal() - unit.ordinal())),
                unit
        );
    }

    @Override
    public int compareTo(MemorySize o) {
        final int unitDiff = unit.ordinal() - o.getUnit().ordinal();
        final int absUnitDiff = Math.abs(unitDiff);

        // compares relatively with their units
        final long thisSize, otherSize;
        if (unitDiff >= 0) {
            thisSize  = this.size * (long) Math.pow(MemorySize.BYTES_PER_KILOBYTE, absUnitDiff);
            otherSize = o.getSize();
        } else {
            thisSize  = this.size;
            otherSize = o.getSize() * (long) Math.pow(MemorySize.BYTES_PER_KILOBYTE, absUnitDiff);
        }

        return Long.compare(thisSize, otherSize);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof MemorySize asMemSize)) return false;
        return size == asMemSize.getSize() && unit == asMemSize.getUnit();
    }

    @Override
    public String toString() {
        return String.format("%d%s", size, unit.name().charAt(0));
    }
}
