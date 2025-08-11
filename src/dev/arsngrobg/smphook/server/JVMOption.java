package dev.arsngrobg.smphook.server;

import dev.arsngrobg.smphook.core.Instance;

/**
 * <p>The {@code JVMOption} interface describes an object that describes a JVM (Java Virtual Machine) command-line argument.</p>
 * 
 * @author Arsngrobg
 * @since  0.1.0
 */
public interface JVMOption extends Instance {
    /** <p>The prefix character for a JVM (Java Virtual Machine) argument.</p> */
    public static final char PREFIX = '-';

    /** <p>The prefix character for a JVM (Java Virtual Machine) option.</p> */
    public static final char OPTION_PREFIX   = 'X';
    /** <p>The prefix character for a JVM (Java Virtual Machine) property.</p> */
    public static final char PROPERTY_PREFIX = 'D';

    /**
     * <p>All JVM (Java Virtual Machine) options should be in the form {@code "-[X/XX]%s"} where the {@code %s} is the name of the JVM argument.</p>
     * 
     * <p>If two {@code JVMOption} instances are equal (as defined by the {@link Instance#equals(Object)} method), then invokations of this method should return equal strings.</p>
     * 
     * @return the fully-qualified name of this JVM option
     */
    public String getFullyQualifiedName();
}
