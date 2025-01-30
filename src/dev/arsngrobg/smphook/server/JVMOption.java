package dev.arsngrobg.smphook.server;

import java.util.Objects;

import dev.arsngrobg.smphook.SMPHookError;

/**
 * <p>The {@code JVMOption} class represents a Java Virtual Machine (JVM) option.</p>
 * 
 * <p>This class serves as a base for different types of JVM options, such as <i>enabled</i>/<i>disabled</i> options, and options with <i>assigned</i> values.
 *    It defines common behaviour with all JVM options.
 * </p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    JVMOption.Enabled
 * @see    JVMOption.Assigned
 * @see    ServerProcess
 */
public abstract sealed class JVMOption permits JVMOption.Enabled, JVMOption.Assigned {
    /**
     * <p>Instantiates an {@code Enabled} JVM option, specified with the {@code option} and internally assigned {@code true}.</p>
     * 
     * @param option - the option string to enable
     * @return an enabled JVM option
     * @throws SMPHookError if {@code option} is {@code null}
     */
    public static JVMOption.Enabled enabled(String option) throws SMPHookError {
        return new Enabled(option, true);
    }

    /**
     * <p>Instantiates an {@code Enabled} JVM option, specified with the {@code option} and internally assigned {@code false}.</p>
     * 
     * @param option - the option string to disable
     * @return an enabled (internally disabled) JVM option
     * @throws SMPHookError if {@code option} is {@code null}
     */
    public static JVMOption.Enabled disabled(String option) throws SMPHookError {
        return new Enabled(option, false);
    }

    /**
     * <p>Instantiates an {@code Asssigned} JVM option, specified with the {@code option} and assign it with the {@code value}.</p>
     * 
     * @param option - the option to assign with {@code value}
     * @param value - the value to assign to the {@code option}
     * @return an assigned JVM option
     * @throws SMPHookError if {@code option} or {@code value} is {@code null}
     */
    public static JVMOption.Assigned assigned(String option, String value) throws SMPHookError {
        return new Assigned(option, value);
    }

    protected final String option;

    // base constructor
    protected JVMOption(String option) throws SMPHookError {
        this.option = SMPHookError.requireNonNull(option, "option");;
    }

    /** @return the option string of this JVM option */
    public String getOption() {
        return option;
    }

    public abstract int hashCode();

    @Override
    public boolean equals(Object obj) {
        if (obj == null)                  return false;
        if (obj == this)                  return true;
        if (getClass() != obj.getClass()) return false;
        JVMOption asOption = (JVMOption) obj;
        return option.equals(asOption.option);
    }

    public abstract String toString();

    /**
     * <p>A Java Virtual Machine (JVM) option with an additional enabled flag.</p>
     * 
     * @author Arsngrobg
     * @since  1.0
     * @see    JVMOption
     */
    public static final class Enabled extends JVMOption {
        private final boolean enabled;

        private Enabled(String option, boolean enabled) {
            super(option);
            this.enabled = enabled;
        }

        /** @return whether this JVM option is enabled */
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public int hashCode() {
            return Objects.hash(option, enabled);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) return false;
            Enabled asOption = (Enabled) obj;
            return enabled == asOption.enabled;
        }

        @Override
        public String toString() {
            return String.format("-XX:%c%s", enabled ? '+' : '-', option);
        }
    }

    /**
     * <p>A Java Virtual Machine (JVM) option with an additional assigned value.</p>
     * 
     * @author Arsngrobg
     * @since  1.0
     * @see    JVMOption
     */
    public static final class Assigned extends JVMOption {
        private final String value;

        private Assigned(String option, String value) throws SMPHookError {
            super(option);
            this.value = SMPHookError.requireNonNull(value, "value");
        }

        /** @return the value that this JVM option is assigned to */
        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(option, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) return false;
            Assigned asOption = (Assigned) obj;
            return value.equals(asOption.value);
        }

        @Override
        public String toString() {
            return String.format("-XX:%s=%s", option, value);
        }
    }
}
