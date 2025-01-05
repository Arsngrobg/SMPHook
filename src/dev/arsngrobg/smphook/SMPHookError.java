package dev.arsngrobg.smphook;

/**
 * <p>The {@code SMPHookError} class is both a manager class and implementation of the {@link java.lang.Error} throwable.</p>
 * <p>All irregular state in this software is recognised as a non-redeemable state and therefore an error is thrown in any unnatural cirumstance.</p>
 * <p>Common states where these errors are thrown are as follows:
 *    <ul>
 *        <li>passing null as arguments into methods that cannot handle null references</li>
 *        <li>passing signed value into a method which requires unsigned values</li>
 *        <li>regex pattern-matching failure</li>
 *        <li>IO errors</li>
 *    </ul>
 * </p>
 * <p>To get a pre-allocated error type from this class, juse invoke {@link SMPHookError#getErr(Type)}.</p>
 * <p>You can throw a generic error using {@link SMPHookError#throwGeneric(String)}.</p>
 * <p>You can throw a generic error with a cause
 * <p>You can throw a null pointer error using {@link SMPHookError#throwNullPointer(String)}.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class SMPHookError extends Error {
    // cache for storing the pre-allocated error objects
    private static final SMPHookError[] CACHE = {
        new SMPHookError(Type.HEAPARG_SIZE_INVALID,           "The size given is an unsigned, non-zero integer."),
        new SMPHookError(Type.HEAPARG_ARGSTR_INVALID,         "The argStr given is an invalid pattern."),
        new SMPHookError(Type.SERVERPROC_NOT_JARFILE,         "The file given is not a .jar file"),
        new SMPHookError(Type.SERVERPROC_JARFILE_NOEXIST,     "The server Jar file does not exist."),
        new SMPHookError(Type.SERVERPROC_JARFILE_INVALID,     "The server Jar file is not valid Minecraft server software."),
        new SMPHookError(Type.SERVERPROC_MISMATCHED_HEAPARGS, "Mismatched heap arguments."),
        new SMPHookError(Type.SERVERPROC_UNUSUAL_STATE,       "Server process in unusual state, forcefully exiting."),
        new SMPHookError(Type.INVALID_DISCORD_WEBHOOK_URL,    "The provided webhook url is invalid."),
        new SMPHookError(Type.INVALID_IPV4_ADDRESS,           "The address string given is not a valid IPv4 string.")
    };

    // simple check for making sure all static types have been cached
    static {
        Type[] types = Type.values();
        if (types.length - 2 != CACHE.length) {
            throw new Error("(DEV_ASSERTION_ERROR): Not all static error references have been allocated.");
        }

        for (int i = 2; i < types.length; i++) {
            if (CACHE[i - 2].type != types[i]) {
                throw new Error("(DEV_ASSERTION_ERROR): Not all static error references have been allocated & duplicate types found.");
            }
        }
    }

    /**
     * <p>Method to retrieve one of the pre-defined error types defined by the {@link SMPHookError.Type} enum.</p>
     * <p>This method throws an {@link IllegalArgumentException} when supplied with {@link Type#GENERIC} or {@link Type#NULL_POINTER}
     *    since generic errors are dynamically allocated and null pointer errors are a special case of generic error.
     * </p>
     * @param type - a type that is within the {@link SMPHookError.Type} enum that is not {@link Type#GENERIC} or {@link Type#NULL_POINTER}
     * @return the dynamically allocated {@code SMPHookError} instance with the given {@code type}
     * @throws IllegalArgumentException if the {@link Type#GENERIC} or {@link Type#NULL_POINTER}
     */
    public static SMPHookError getErr(Type type) throws IllegalArgumentException {
        if (type == null) throw new IllegalArgumentException("Enum value 'type' cannot be a null reference.");

        switch (type) {
            case Type.GENERIC:
                throw new IllegalArgumentException("Cannot throw GENERIC ERRORS using this method - use throwGeneric(String) instead.");
            case Type.NULL_POINTER:
                throw new IllegalArgumentException("NULL_POINTER errors are a special case of generic error - use throwNullPointer(String) instead.");
            default:
                return CACHE[type.ordinal() - 2];
        }
    }

    /**
     * <p>Throws a {@link SMPHookError.Type#GENERIC} error with the specified error message ({@code message}).</p>
     * @param message - the message to be displayed when thrown
     * @throws SMPHookError
     */
    public static void throwGeneric(String message) throws SMPHookError {
        throw new SMPHookError(Type.GENERIC, message);
    }

    /**
     * <p>Throws a {@link SMPHookError.Type#GENERIC} error with the specified {@code cause} {@link Throwable}.</p>
     * @param cause - the throwable that caused this error.
     * @throws SMPHookError
     */
    public static void throwWithCause(Throwable cause) throws SMPHookError {
        throw new SMPHookError(Type.GENERIC, String.format("Caused by %s.", cause.getClass().getSimpleName()));
    }

    /**
     * <p>Throws a {@link SMPHookError.Type#NULL_POINTER} error with a error message displaying that the {@code varNames} are {@code null}.</p>
     * <b>This is a special case of a generic error.</b>
     * @param varNames - the variable name(s) to display in the error message - can be left empty (throws a generic message instead)
     * @throws SMPHookError
     */
    @SafeVarargs
    public static void throwNullPointer(String... varNames) throws SMPHookError {
        String errMsg;

        if (varNames.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String varName : varNames) {
                sb.append("'").append(varName).append("'").append(", ");
            }
            sb.setLength(sb.length() - 2); // remove trailing comma
            sb.append(" cannot be null.");
            errMsg = sb.toString();
        } else errMsg = "Null references disallowed";

        throw new SMPHookError(Type.NULL_POINTER, errMsg);
    }

    /**
     * <p>The different types of errors that are available in this class.</p>
     * <p>{@code GENERIC} errors are dynamically instantiated, and display custom error messages.</p>
     * <p>{@code NULL_POINTER} errors are a special case of {@code GENERIC}, specific to null references.</p>
     * <p>The rest are all statically allocated.</p>
     */
    public static enum Type {
        GENERIC,                // throw generic error - for testing purposes
        NULL_POINTER,           // throw for null pointers

        HEAPARG_SIZE_INVALID,
        HEAPARG_ARGSTR_INVALID,

        SERVERPROC_NOT_JARFILE,
        SERVERPROC_JARFILE_NOEXIST,
        SERVERPROC_JARFILE_INVALID,
        SERVERPROC_MISMATCHED_HEAPARGS,
        SERVERPROC_UNUSUAL_STATE,

        INVALID_DISCORD_WEBHOOK_URL,

        INVALID_IPV4_ADDRESS
    }

    private final String message;
    private final Type   type;

    private SMPHookError(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /** @return the type of error that this instance is */
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        SMPHookError asErr = (SMPHookError) obj;
        return type == asErr.type && message.equals(asErr.message);
    }

    @Override
    public String toString() {
        return String.format("(%s): %s", type, message);
    }
}
