package dev.arsngrobg.smphook.core;

/**
 * <p>This interface must be implemented by all instanciable classes in the <b>SMPHook</b> system.</p>
 * 
 * <p>It ensures that every instanciable type will implement its own {@link #hashCode()}, {@link #equals(Object)}, and {@link #toString()} methods.</p>
 * 
 * @author Arsngrobg
 * @since  0.1.0
 */
public interface Instance {
    /**
     * <p>Provides a relatively unique values for this instance type.
     *    Such that, for two given values that are objectively equal to eachother (i.e. {@link #equals(Object)}), the hashcode for both instances are also equal to eachother.
     *    So, in a sense:
     *    <pre><blockquote>
     *       [ o1.equals(o2) ] === [ o1.hashCode() == o2.hashCode() ]
     *    </blockquote></pre>
     * </p>
     * 
     * @return the hashcode of this instance
     * 
     * @since 0.1.0
     */
    @Override
    int hashCode();

    /**
     * <p>Compares this object to the supplied {@code obj}.</p>
     * 
     * <p>For this method to be considered a valid {@link #equals(Object)} implementation:
     *    <ul>
     *       <li>it must be reflective - {@code o1.equals(o2) == o2.equals(o1)}</li>
     *       <li>it must only compare to to objects of the same type as this object</li>
     *       <li>{@code o1.equals(null)} must always evaluate to {@code false}</li>
     *    </ul>
     * </p>
     * 
     * @param  obj - the object to compare this object to
     * @return {@code true} if this object is equal to this object, {@code false} if otherwise
     * 
     * @since 0.1.0
     */
    @Override
    boolean equals(Object obj);

    /**
     * <p>Formats this object into its {@code String} representation.</p>
     * 
     * @return the {@code String} representation of this {@code Instance}
     * 
     * @since 0.1.0
     */
    @Override
    String toString();
}
