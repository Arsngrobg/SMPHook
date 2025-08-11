package dev.arsngrobg.smphook.core;

/**
 * <p>Tagging interface to define that an {@code Object} or {@code Instance} is {@code Representable}.</p>
 * 
 * <p><i>Wraps the {@link Object#toString()} instance method.</i></p>
 * 
 * @author Arsngrobg
 * @since  0.0.1
 */
public interface Representable {
    /**
     * <p>Returns the {@code String} representation of this object.</p>
     * 
     * <p>This method should return an accurate textual representation of this object.</p>
     * 
     * @return the {@code String} representation of this object
     * 
     * @since 0.0.1
     */
    @Override
    String toString();
}
