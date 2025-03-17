package dev.arsngrobg.smphook.server;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;
import static dev.arsngrobg.smphook.SMPHookError.nullCondition;

/**
 * <p>The {@code JVMOption} interface represents an experimental Java Virtual Machine (JVM) option.</p>
 * 
 * <p>This interface serves as a base for different types of JVM options, such as <b>enabled</b>/<b>disabled</b>, and options with <b>assigned</b> values.
 *    It defines common behaviour with all JVM options.
 * </p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    #enabled(String)
 * @see    #disabled(String)
 * @see    #assigned(String, Object)
 * @see    ServerProcess
 */
public sealed interface JVMOption permits JVMOption.Enabled, JVMOption.Assigned {
    /**
     * <p>Constructs an <b>enabled</b> {@code JVMOption} with the supplied {@code optionName}.</p>
     * 
     * @param optionName - the name of the experimental JVM option
     * @return an <b>enabled</b> {@code JVMOption} instance
     * @throws SMPHookError if {@code optionName} is {@code null}
     */
    public static JVMOption.Enabled enabled(String optionName) throws SMPHookError {
        return new Enabled(SMPHookError.strictlyRequireNonNull(optionName, "optionName"), true);
    }

    /**
     * <p>Constructs a <b>disabled</b> {@code JVMOption} with the supplied {@code optionName}.</p>
     * 
     * @param optionName - the name of the experimental JVM option
     * @return a <b>disabled</b> {@code JVMOption} instance
     * @throws SMPHookError if {@code optionName} is {@code null}
     */
    public static JVMOption.Enabled disabled(String optionName) throws SMPHookError {
        return new Enabled(SMPHookError.strictlyRequireNonNull(optionName, "optionName"), false);
    }

    /**
     * <p>Constructs an <b>assigned</b> {@code JVMOption} with the supplied {@code optionName} and {@code value}.
     *    The generic {@code value} object 
     * </p>
     * 
     * @param optionName - the name of the experimental JVM option
     * @param      value - the value to assign to the JVM option
     * @return a <b>assigned</b> {@code JVMOption} instance with the supplied {@code value}
     * @throws SMPHookError if {@code optionName} or {@code value} is {@code null}
     */
    public static JVMOption.Assigned assigned(String optionName, Object value) throws SMPHookError {
        SMPHookError.caseThrow(
            nullCondition(optionName, "optionName"),
            nullCondition(value, "value")
        );

        return new Assigned(optionName, String.valueOf(value));
    }

    /** @return the name of the experimental JVM option */
    String getOptionName();

    /**
     * <p><i>This class is an implementation of the {@link JVMOption} interface.</i></p>
     * 
     * <p>This class comes with an additional {@code enabed} flag.</p>
     * 
     * @author Arsngrobg
     * @since  1.0
     * @see    JVMOption
     */
    public static final class Enabled implements JVMOption {
        private final String name;
        private final boolean enabled;

        private Enabled(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }

        @Override
        public String getOptionName() {
            return name;
        }

        /** @return whether this Java Virtual Machine (JVM) option is enabled */
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public int hashCode() {
            return SMPHook.hashOf(name, enabled);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj.getClass() != getClass()) return false;
            Enabled asEnabledOption = (Enabled) obj;
            return enabled == asEnabledOption.enabled && name.equals(asEnabledOption.name);
        }

        @Override
        public String toString() {
            return String.format("-XX:%c%s", enabled ? '+' : '-', name);
        }
    }

    /**
     * <p><i>This class is an implementation of the {@link JVMOption} interface.</i></p>
     * 
     * <p>This class comes with an additional {@code value} field.</p>
     * 
     * @author Arsngrobg
     * @since  1.0
     * @see    JVMOption
     */
    public static final class Assigned implements JVMOption {
        private final String name;
        private final String value;

        private Assigned(String name, String value) {
            this.name  = name;
            this.value = value;
        }

        @Override
        public String getOptionName() {
            return name;
        }

        /** @return the value assigned to this Java Virtual Machine (JVM) option */
        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return SMPHook.hashOf(name, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj.getClass() != getClass()) return false;
            Assigned asAssignedOption = (Assigned) obj;
            return value.equals(asAssignedOption.value) && name.equals(asAssignedOption.name);
        }

        @Override
        public String toString() {
            return String.format("-XX:%s=%s", name, value);
        }
    }
}
