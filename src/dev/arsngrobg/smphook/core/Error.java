package dev.arsngrobg.smphook.core;

/**
 * <p></p>
 *
 * @author Arsngrobg
 * @since  0.0.1
 */
public final class Error extends java.lang.Error implements Instance {
    public enum Type {
        /** <p>The most basic form of error.</p> */
        GENERIC,
        /** <p>An error caused by a {@link java.lang.Throwable}.</p> */
        PROPAGATED,
        /** <p>An error caused by file I/O operations.</p> */
        FILE
    }
}
