package dev.arsngrobg.smphook.core;

/**
 * <p></p>
 *
 * @author Arsngrobg
 * @since  0.0.3
 */
public final class SMPHookError extends Error implements Instance {
    /**
     * <p>The type of error.</p>
     *
     * @author Arsngrobg
     * @since  0.0.3
     */
    public enum Type {
        /** <p>The most basic form of error.</p> */
        GENERIC,
        /** <p>An error caused by invalid {@code null} values.</p> */
        NULL_REFERENCE,
        /** <p>An error caused by a {@link java.lang.Throwable}.</p> */
        PROPAGATED,
        /** <p>An error caused by file I/O operations.</p> */
        FILE
    }

    /**
     * <p>Like {@link SMPHookError#}, this method checks to see whether the provided {@code value} is not {@code null}.</p>
     *
     * @param <T>   the type of the {@code value} and {@code alt} object
     * @param value the object to test for null-ness
     * @param alt   the substitute object if {@code value} is {@code null}
     * @return      {@code value} or {@code alt}
     * @throws SMPHookError
     *
     * @since 0.0.3
     * @see   SMPHookError#strictlyRequireNonNull(Object, String)
     */
    public static <T> T requireNonNull(T value, T alt) throws SMPHookError {
        return (value != null) ? value : SMPHookError.strictlyRequireNonNull(alt, "alt");
    }

    /**
     * <p>Checks to see whether the provided {@code value} is not {@code null}.</p>
     * <p>If {@code value} is {@code null}, then this method will throw an {@code SMPHookError}.</p>
     *
     * @param  <T>   the type of the {@code value} parameter
     * @param  value the object to test for null-ness
     * @param  id    the <i>optional</i> name to provide if {@code value} is {@code null}
     * @return       the {@code value} - if not {@code null}
     * @throws SMPHookError if {@code value} is {@code null}
     *
     * @since 0.0.3
     */
    public static <T> T strictlyRequireNonNull(T value, String id) throws SMPHookError {
        if (value != null) return value;

        String errMsg;
        if (id == null || id.isEmpty() || id.isBlank()) {
            errMsg = "Invalid null reference.";
        } else {
            errMsg = String.format("'%s' cannot be null.", id);
        }

        throw SMPHookError.with(Type.NULL_REFERENCE, errMsg);
    }

    /**
     * <p></p>
     *
     * @param  type
     * @param  message 
     * @return
     * @throws SMPHookError if {@code type} is {@code null}
     *
     * @since 0.0.3
     */
    public static SMPHookError with(Type type, String message) throws SMPHookError {
        message = (message.isEmpty() || message.isBlank()) ? null : message;
        return new SMPHookError(
            SMPHookError.strictlyRequireNonNull(type, "type"),
            SMPHookError.requireNonNull        (message, "An error has occurred")
        );
    }

    private final Type   type;
    private final String message;

    private SMPHookError(Type type, String message) {
        this.type    = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Hashable.hashOf(type, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SMPHookError asErr)) return false;
        return type == asErr.type && message.equals(asErr.message);
    }

    @Override
    public String toString() {
        return String.format("message");
    }
}
