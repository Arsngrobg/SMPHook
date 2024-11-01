package arsngrobg.smphook;

/**
 * Error type that SMPHook throws.
 * This class stores static instances of each error type defined by the internal {@link SMPHookError.Type} enum.
 * To get an error of a particular type is done by invoking the {@link SMPHookError#get(Type)} method.
 */
public final class SMPHookError extends Error {
    /** Error types that SMPHook can throw. */
    public static enum Type {
        NULL_POINTER,
        ILLEGAL_HEAP_ARGUMENT_SIZE,
        INVALID_HEAP_ARGUMENT_FORMAT
    };

    private static final SMPHookError[] CACHE = {
        new SMPHookError(Type.NULL_POINTER,                 "Null value disallowed."),
        new SMPHookError(Type.ILLEGAL_HEAP_ARGUMENT_SIZE,   "Heap argument size provided cannot be negative OR zero."),
        new SMPHookError(Type.INVALID_HEAP_ARGUMENT_FORMAT, "Argument string provided is not a valid JVM heap argument.")
    };

    // assertion for unimplemented errors for error types
    static { if (CACHE.length != Type.values().length) throw new AssertionError("Unimplemented errors."); }

    /**
     * Retrieves the error type given by the {@code type} argument.
     * @param type - the type of instance of {@link SMPHookError} to get
     * @return the correlating {@link SMPHookError} error
     */
    public static SMPHookError get(Type type) {
        return CACHE[type.ordinal()];
    }

    private final Type   type;
    private final String message;

    private SMPHookError(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    /** @return the type of error this is */
    public Type getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public int hashCode() {
        return type.ordinal();
    }
}
