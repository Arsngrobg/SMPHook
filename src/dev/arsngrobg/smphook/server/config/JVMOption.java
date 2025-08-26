package dev.arsngrobg.smphook.server.config;

import java.util.Arrays;

import dev.arsngrobg.smphook.core.Hashable;
import dev.arsngrobg.smphook.core.Instance;

// TODO: ADD FACTORIES

/**
 * <p>The {@code JVMOption} class represents a configurable component of the JVM using the CLI.</p>
 *
 * @author Arsngrobg
 * @since  0.1.0
 */
public final class JVMOption<T> implements Instance {
    /**
     * <p>The types of JVM options available.</p>
     *
     * @author Arsngrobg
     * @since  0.1.0
     */
    public enum Type {
        STANDARD,
        NON_STANDARD,
        ADVANCED;

        /**
         * <p>The prefix string determines the output of the {@link JVMOption#toString()} method.</p>
         *
         * @return the prefix string for this {@code Type} enum
         *
         * @since 0.1.0
         */
        public String getPrefix() {
            return switch (this) {
                case STANDARD     -> "-";
                case NON_STANDARD -> "-X";
                case ADVANCED     -> "-XX:";
            };
        }
    }

    /**
     * <p>The key/value seperators.</p>
     *
     * @author Arsngrobg
     * @since  0.1.0
     */
    public enum ValueSeperator {
        NONE,
        COLON,
        EQUALS;

        /**
         * <p>The seperator character determines the output of the {@link JVMOption#toString()} method.</p>
         *
         * @return the key/value seperator character for this {@code ValueSeperator} enum
         *
         * @since 0.1.0
         */
        public char getChar() {
            return switch (this) {
                case NONE   -> '\0';
                case COLON  -> ':';
                case EQUALS -> '=';
            };
        }
    }

    /** <p>The character that prefixes the option {@code name}.</p> */
    public static final char SYSTEM_PROPERTY_CHAR = 'D';

    private final Type           type;
    private final String         name;
    private final ValueSeperator seperator;
    private final T              value;

    private JVMOption(Type type, String name, ValueSeperator seperator, T value) {
        this.type      = type;
        this.name      = name;
        this.seperator = seperator;
        this.value     = value;
    }

    /**
     * <p>This {@code JVMOption} is a system property if the option {@code name} starts with the {@code 'D'} character.</p>
     *
     * @return {@code true} if this {@code JVMOption} is a system property; {@code false} if otherwise
     *
     * @since 0.1.0
     */
    public boolean isSystemProperty() {
        return type == Type.STANDARD && name.charAt(0) != SYSTEM_PROPERTY_CHAR;
    }

    /**
     * <p>The type of {@code JVMOption} determines the prefix string.</p>
     *
     * @return the type of this {@code JVMOption}
     *
     * @since 0.1.0
     * @see   JVMOption.Type
     */
    public Type getType() {
        return type;
    }

    /**
     * <p>The key value of this {@code JVMOption}.</p>
     *
     * @return the name of this {@code JVMOption}
     *
     * @since 0.1.0
     */
    public String getName() {
        return name;
    }

    /**
     * <p>The key/value sepeperator determines the delimiter used to seperate keys and the appropriate value.</p>
     * <p><i>NOTE: This value is ignored if the {@code value} is a {@link java.lang.Boolean}.</i></p>
     *
     * @return the key/value seperator of this {@code JVMOption}
     *
     * @since 0.1.0
     * @see   ValueSeperator
     */
    public ValueSeperator getSeperator() {
        return seperator;
    }

    /**
     * <p>The value that is associated with the option {@code name}.</p>
     * <p>Simple flags use the {@link java.lang.Void} type.</p>
     * <p>Options with the {@link java.lang.Boolean} type are prefixed with {@code '-'}/{@code '+'}.</p>
     *
     * @return the value of this {@code JVMOption}
     *
     * @since 0.1.0
     */
    public T getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Hashable.hashOf(type, name, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof JVMOption<?> asOption)) return false;
        if (value.getClass() != asOption.value.getClass()) return false; // value.equals(asOption.value) can fail if either are null
        return type == asOption.type && name.equals(asOption.name) && value.equals(asOption.value);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(type.getPrefix());
        if (value instanceof Boolean bool) { // ignore ValueSeperator if T is Boolean
            stringBuilder.append(bool ? '+' : '-');
        }
        stringBuilder.append(name).append(seperator.getChar());
        if (value != null && !(value instanceof Boolean)) {
            stringBuilder.append(value);
        }

        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        // TODO: TESTING
        // -Dprop="Hello, World!" -Xms1g -Xmx8g -server -verbose:gc -javaagent:/path/to/agent.jar -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:+UseStringDeduplication -XX:G1NewSizePercent=20 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:+UseNUMA -XX:+UseCompressedOops
        JVMOption<?>[] options = {
            new JVMOption<String> (Type.STANDARD,     "Dprop",                       ValueSeperator.EQUALS, "Hello, World!"),
            new JVMOption<String> (Type.NON_STANDARD, "ms",                          ValueSeperator.NONE,   "1g"),
            new JVMOption<String> (Type.NON_STANDARD, "mx",                          ValueSeperator.NONE,   "8g"),
            new JVMOption<Void>   (Type.STANDARD,     "server",                      ValueSeperator.NONE,   null),
            new JVMOption<String> (Type.STANDARD,     "verbose",                     ValueSeperator.COLON,  "gc"),
            new JVMOption<String> (Type.STANDARD,     "javaagent",                   ValueSeperator.COLON,  "/path/to/agent.jar"),
            new JVMOption<Boolean>(Type.ADVANCED,     "UnlockExperimentalVMOptions", ValueSeperator.NONE,   true),
            new JVMOption<Boolean>(Type.ADVANCED,     "UseG1GC",                     ValueSeperator.NONE,   true),
            new JVMOption<Boolean>(Type.ADVANCED,     "UseStringDeduplication",      ValueSeperator.NONE,   true),
            new JVMOption<Integer>(Type.ADVANCED,     "G1NewSizePercent",            ValueSeperator.EQUALS, 20),
            new JVMOption<Integer>(Type.ADVANCED,     "G1MaxNewSizePercent",         ValueSeperator.EQUALS, 40),
            new JVMOption<String> (Type.ADVANCED,     "G1HeapRegionSize",            ValueSeperator.EQUALS, "8M"),
            new JVMOption<Boolean>(Type.ADVANCED,     "UseNUMA",                     ValueSeperator.NONE,   true),
            new JVMOption<Boolean>(Type.ADVANCED,     "UseCompressedOops",           ValueSeperator.NONE,   true)
        };

        Arrays.asList(options).forEach(System.out::println);
    }
}
