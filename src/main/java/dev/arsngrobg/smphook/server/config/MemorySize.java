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
 * @see     MemorySize#ofSize(long, Unit)
 * @see     MemorySize#ofBytes(long)
 * @see     MemorySize#fromString(String) 
 */
public final class MemorySize {
    /**
     * <p>This is the common factory for defining a {@code MemorySize} value.</p>
     * <p>It parses the provided {@code memStr} into a tangible {@code MemorySize} object.
     *    <pre><code>
     *        var size = MemorySize.fromString("20"); // infers that you want to represent 20 bytes
     *        System.out.println(size); // outputs: "20B"
     *        System.out.println(size.equals(MemorySize.fromString("20B")); // outputs: true
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
    // TODO: handle illegal unit suffix
    public static MemorySize fromString(final String memStr) {
        if (Objects.isNull(memStr)) throw new NullPointerException("memStr");
        if (memStr.isBlank()) throw new IllegalArgumentException("memStr cannot be blank");

        final char lastChar = memStr.charAt(memStr.length() - 1);
        final Optional<Unit> maybeUnit = Stream.of(Unit.values())
                                               .filter(u -> u.name().charAt(0) == lastChar)
                                               .findFirst();

        final int subStrEnd = maybeUnit.isPresent() ? memStr.length() - 1 : memStr.length();
        final long size = Integer.parseInt(memStr.substring(0, subStrEnd));

        return maybeUnit.map(u -> MemorySize.ofSize(size, u))
                        .orElse(MemorySize.ofBytes(size));
    }

    /**
     * <p>This is a factory for creating a {@code MemorySize} from a number of {@code bytes}.</p>
     * <p>This factory will implicitly convert the {@code bytes} into the closest and most-compressed representation of
     *    the provided {@code bytes}.
     *    <pre><code>
     *        var bytes = MemorySize.ofBytes(1024);
     *        System.out.println(bytes); // outputs: "1K"
     *    </code></pre>
     * </p>
     *
     * @param  bytes the number of whole bytes that this {@code MemorySize} will carry
     * @return       a new {@code MemorySize} object, consisting of the number of provided {@code bytes} compressed into
     *               the smallest representation possible
     * @author       Arsngrobg
     * @since        v0.0.1-pre_alpha
     */
    // TODO: compress whenever possible
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
