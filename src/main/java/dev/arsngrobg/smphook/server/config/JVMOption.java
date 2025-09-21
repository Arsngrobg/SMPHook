package dev.arsngrobg.smphook.server.config;

import java.util.Arrays;
import java.util.Objects;

/**
 * <p>The {@code JVMOption} data class represents a tangible object-representation of a Java Virtual Machine option.</p>
 * <p>This dataclass provides a way to model JVM options pre-initialisation of a {@link ServerProcess}.</p>
 * <p>It exposes a single factory method for creating a {@code JVMOption} instance:
 *    <pre><code>
 *        var option = JVMOption.parse("-XX:+UnlockExperimentalVMOptions");
 *        System.out.println(option.getCompliance());     // output: Compliance.NON_STANDARD
 *        System.out.println(option.getName());           // output: UnlockExperimentalVMOptions
 *        System.out.println(option.getValueSeparator()); // output: ValueSeparator.NONE
 *        System.out.println(option.getValue());          // output: true
 *    </code></pre>
 *    All {@code JVMOption}s with a compliance level of {@link Compliance#NON_STANDARD} and is a {@code boolean} flag
 *    (see {@link JVMOption#isAdvancedBooleanFlag()}) then a {@code '+'} or {@code '-'} character is prepended to the
 *    option name instead of {@code valueSeparator + value}.
 * </p>
 *
 * @param  <T> the type of the {@code value} it holds
 * @author     Arsngrobg
 * @since      v0.0.2-pre_alpha
 * @see        JVMOption#parse(String)
 */
public final class JVMOption<T> {
    /**
     * <p>The {@code Compliance} enum is the set of levels that a JVM option can have.</p>
     * <p>{@link Compliance#STANDARD} options are common, mostly generic options that tweak small things of the JVM.
     *    {@link Compliance#ADVANCED} options are for fine-tuning JVM performance. {@link Compliance#NON_STANDARD}
     *    options are not unanimous with all implementations of the JVM and may have no effect during runtime.
     * </p>
     *
     * @author Arsngrobg
     * @since  v0.0.2-pre_alpha
     */
    public enum Compliance {
        STANDARD,
        NON_STANDARD,
        ADVANCED;

        /**
         * <p>Returns the prefix string for this {@code Compliance} level.</p>
         *
         * @return the prefix string for this {@code Compliance} level
         * @author Arsngrobg
         * @since  v0.0.2-pre_alpha
         */
        String getPrefix() {
            return switch (this) {
                case STANDARD     -> "-";
                case NON_STANDARD -> "-X";
                case ADVANCED     -> "-XX:";
            };
        }
    }

    /**
     * <p>The {@code ValueSeparator} enum is a strongly-typed definition of a set of value separators for a
     *    {@code JVMOption}.
     * </p>
     * <p>Each {@code ValueSeparator} has an associated character (except from {@link ValueSeparator#NONE}) that is a
     *    delimiter between the {@code JVMOption}'s {@code name} and {@code value}. For the case of
     *    {@link ValueSeparator#NONE} it does not have a character (as it is the typed variant of {@code null} for
     *    {@code ValueSeparator}s), to prevent illegal character conversion use the {@link ValueSeparator#isNotNone()}
     *    property.
     * </p>
     *
     * @author Arsngrobg
     * @since  v0.0.2-pre_alpha
     * @see    ValueSeparator#isNotNone()
     * @see    ValueSeparator#getChar()
     */
    public enum ValueSeparator {
        NONE,
        COLON,
        EQUALS;

        /**
         * <p>Property that returns whether this {@code ValueSeparator} is <b>not</b> the {@link ValueSeparator#NONE}
         *    value.
         * </p>
         * <p>This property is useful for testing pre-invocation of the {@link ValueSeparator#getChar()} method.</p>
         *
         * @return {@code true} if this {@code ValueSeparator} is true to the property stated above; {@code false} if
         *         otherwise
         * @author Arsngrobg
         * @since  v0.0.2-pre_alpha
         */
        boolean isNotNone() {
            return this != ValueSeparator.NONE;
        }

        /**
         * <p>Returns the character that this {@code ValueSeparator} represents.</p>
         * <p>If this {@code ValueSeparator} is {@link ValueSeparator#NONE} then the method will throw an
         *    {@link IllegalArgumentException} - as it does not have a character. Always test this
         *    {@code ValueSeparator} using the {@link ValueSeparator#isNotNone()} property.
         * </p>
         *
         * @return the character that this {@code ValueSeparator} represents
         * @author Arsngrobg
         * @since  v0.0.2-pre_alpha
         * @see    ValueSeparator#isNotNone()
         */
        char getChar() throws IllegalArgumentException {
            return switch (this) {
                case NONE   -> throw new IllegalArgumentException("ValueSeparator.NONE has no character.");
                case COLON  -> ':';
                case EQUALS -> '=';
            };
        }
    }

    private final Compliance     compliance;
    private final String         name;
    private final ValueSeparator valueSeparator;
    private final T              value;

    private JVMOption(final Compliance compliance, final String name, final ValueSeparator valueSeparator, final T value) {
        this.compliance     = compliance;
        this.name           = name;
        this.valueSeparator = valueSeparator;
        this.value          = value;
    }

    /**
     * <p>Property that returns whether this {@code JVMOption} holds a value.</p>
     * <p>This property determines how this object is represented as a string.
     *    <pre><code>
     *        var withoutValue = JVMOption.parse("-verbose:gc");
     *        System.out.println(withoutValue.hasValue()); // output: true
     *
     *        var withValue = JVMOption.parse("-server");
     *        System.our.println(option.hasValue()); // output: false
     *    </code></pre>
     * </p>
     *
     * @return {@code true} if this {@code JVMOption} is true to the property stated above; {@code false} if otherwise
     * @author Arsngrobg
     * @since  v0.0.2-pre_alpha
     */
    public boolean hasValue() {
        return value != null;
    }

    /**
     * <p>Property that returns whether this {@code JVMOption} has {@link Compliance#ADVANCED} and holds a
     *    {@code boolean} value.
     * </p>
     * <p>This property determines how this object is represented as a string.
     *    <pre><code>
     *        var option = JVMOption.parse("-XOption=true");
     *        System.out.println(option.isAdvancedBooleanFlag()); // output: false
     *
     *        var advancedOption = JVMOption.parse("-XX:+Option");
     *        System.out.println(advancedOption.isAdvancedBooleanFlag()); // output: true
     *    </code></pre>
     * </p>
     *
     * @return {@code true} if this {@code JVMOption} is true to the property stated above; {@code false} if otherwise
     * @author Arsngrobg
     * @since  v0.0.2-pre_alpha
     */
    public boolean isAdvancedBooleanFlag() {
        return (compliance == Compliance.ADVANCED) && (value instanceof Boolean);
    }

    /**
     * <p>Returns the compliance type of this {@code JVMOption}.</p>
     * <p>The {@link Compliance} type determines the prefix string attached to the string representation, and vice
     *    versa.
     * </p>
     *
     * @return the compliance type of this {@code JVMOption}
     * @author Arsngrobg
     * @since  v0.0.2-pre_alpha
     */
    public Compliance getCompliance() {
        return compliance;
    }

    /**
     * <p>Returns the name of this {@code JVMOption}.</p>
     * <p>This is the identifying characteristic of the {@code JVMOption}.
     *    For all the available {@code JVMOption}s for your specific JVM, execute:
     *    <pre><code>
     *        $ java -XX:+PrintFlagsFinal
     *    </code></pre>
     * </p>
     *
     * @return the name of this {@code JVMOption}
     * @author Arsngrobg
     * @since  v0.0.2-pre_alpha
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Returns the {@link ValueSeparator} for this {@code JVMOption}.</p>
     * <p>This value is ignored if the {@link JVMOption#isAdvancedBooleanFlag()} property is {@code true}.</p>
     *
     * @return the {@link ValueSeparator} for this {@code JVMOption}
     * @author Arsngrobg
     * @since  v0.0.2-pre_alpha
     * @see    JVMOption#isAdvancedBooleanFlag()
     */
    public ValueSeparator getValueSeparator() {
        return valueSeparator;
    }

    /**
     * <p>Returns the value held by this {@code JVMOption}.</p>
     * <p>If there is no value present ({@code value == null}) then this method will throw an
     *    {@link IllegalAccessError}.
     * </p>
     *
     * @return the value held by this {@code JVMOption}
     * @author Arsngrobg
     * @since  v0.0.2-pre_alpha
     * @see    JVMOption#hasValue()
     */
    public T getValue() throws IllegalAccessError {
        if (!hasValue()) throw new IllegalAccessError(String.format("The JVMOption \"%s\" has no value.", this));
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, compliance, valueSeparator);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof JVMOption<?> asOption)) return false;
        return (
                compliance == asOption.getCompliance()         &&
                valueSeparator == asOption.getValueSeparator() &&
                name.equals(asOption.getName())                &&
                value.equals(asOption.getValue())
        );
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(compliance.getPrefix());
        if (isAdvancedBooleanFlag()) {
            stringBuilder.append((boolean) value ? '+' : '-').append(name);
        } else {
            stringBuilder.append(name);
            if (hasValue()) {
                if (valueSeparator.isNotNone()) stringBuilder.append(valueSeparator.getChar());
                if ((value instanceof String str) && (str.contains(" "))) {
                    stringBuilder.append("\"").append(value).append("\"");
                } else stringBuilder.append(value);
            }
        }
        return stringBuilder.toString();
    }

        public static void main(String[] args) {
            JVMOption<?>[] options = {
                new JVMOption<>    (Compliance.STANDARD,     "Dprop",                       ValueSeparator.EQUALS,"Hello, World!"),
                new JVMOption<>    (Compliance.NON_STANDARD, "ms",                          ValueSeparator.NONE,  "1g"),
                new JVMOption<>    (Compliance.NON_STANDARD, "mx",                          ValueSeparator.NONE,  "8g"),
                new JVMOption<Void>(Compliance.STANDARD,     "server",                      ValueSeparator.NONE,   null),
                new JVMOption<>    (Compliance.STANDARD,     "verbose",                     ValueSeparator.COLON,  "gc"),
                new JVMOption<>    (Compliance.STANDARD,     "javaagent",                   ValueSeparator.COLON,  "/path/to/agent.jar"),
                new JVMOption<>    (Compliance.ADVANCED,     "UnlockExperimentalVMOptions", ValueSeparator.NONE,   true),
                new JVMOption<>    (Compliance.ADVANCED,     "UseG1GC",                     ValueSeparator.NONE,   true),
                new JVMOption<>    (Compliance.ADVANCED,     "UseStringDeduplication",      ValueSeparator.NONE,   true),
                new JVMOption<>    (Compliance.ADVANCED,     "G1NewSizePercent",            ValueSeparator.EQUALS, 20),
                new JVMOption<>    (Compliance.ADVANCED,     "G1MaxNewSizePercent",         ValueSeparator.EQUALS, 40),
                new JVMOption<>    (Compliance.ADVANCED,     "G1HeapRegionSize",            ValueSeparator.EQUALS, "8M"),
                new JVMOption<>    (Compliance.ADVANCED,     "UseNUMA",                     ValueSeparator.NONE,   true),
                new JVMOption<>    (Compliance.ADVANCED,     "UseCompressedOops",           ValueSeparator.NONE,   true)
        };

        Arrays.asList(options).forEach(System.out::println);
    }
}
