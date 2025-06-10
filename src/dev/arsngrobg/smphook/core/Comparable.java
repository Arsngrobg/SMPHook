package dev.arsngrobg.smphook.core;

/**
 * <p>Tagging interface to define that an {@code Object} or {@code Instance} is {@code Comparable} with other {@code Instance}s.</p>
 * 
 * <p><i>Wraps the {@link Object#equals(Object)} instance method.</i></p>
 * 
 * @author Arsngrobg
 * @since  0.1.0
 */
public interface Comparable {
    /**
     * <p>Returns whether this object is equal to the supplied object {@code o}.</p>
     * 
     * <p>This method must implement valid comparisons between non-null references.
     *    <ul>
     *       <li>It is reflexive - {@code o.equals(o) == true}</li>
     *       <li>It is symmetric - {@code x.equals(y) == y.equals(x)}</li>
     *       <li>It is transistive - {@code x.equals(y) == y.equals(z) == x.equals(z)}</li>
     *       <li>It is consistent - {@code x.equals(y) == true} <b>always</b></li>
     *    </ul>
     * </p>
     * 
     * <p>This method should perform a necessary lookup & comparison between fields of the object.</p>
     * 
     * @param o - the object to compare to this
     * 
     * @return whether this object is equal to {@code o}
     * 
     * @since 0.1.0
     */
    @Override
    boolean equals(Object o);
}
