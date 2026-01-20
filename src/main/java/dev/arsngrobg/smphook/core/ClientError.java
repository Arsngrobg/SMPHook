package dev.arsngrobg.smphook.core;

import java.util.Objects;

/**
 * <p>The {@code ClientError} class represents an illegal state of the <b>SMPHook</b> system.</p>
 * <p>This class extends the {@link java.lang.Error} class. Hence, we assume that any illegal state of <b>SMPHook</b> is
 *    considered illegal in the broader terms of the program and runtime. So, we kill on any illegal state we encounter.
 * </p>
 *
 * @author  Arnsogrbg
 * @since   v0.0.2-pre-alpha
 * @version v1.0
 */
public final class ClientError extends Error {
    private static final String DEFAULT_NULL_REFERENCE_MESSAGE = "Illegal null reference.";

    /**
     * <p>Checks whether the reference to {@code obj} is {@code null}, and throws a {@code ClientError} if such is the
     *    case.
     *    <pre><code>
     *        var obj     = ...; // obtained through some arbitrary code
     *        var nonNull = ClientError.requireNonNull(obj, "obj");
     *        // any operations are all valid now as it has passed the null check
     *    </code></pre>
     *    <pre><code>
     *        Object obj  = null;
     *        var nonNull = ClientError.requireNonNull(obj, "obj");
     *        // output: ClientError[Type: NULL_REFERENCE, Message: "obj" cannot be null.]
     *    </code></pre>
     * </p>
     *
     * @param  obj         the object reference to check for nullability
     * @param  name        the name of the object reference for correct error messaging
     * @return             the input object reference, if it is not {@code null}
     * @param  <T>         the type of the object reference
     * @throws ClientError if {@code obj} is {@code null}
     * @author             Arsngrobg
     * @since              v0.0.2-pre-alpha
     */
    public static <T> T requireNonNull(T obj, String name) throws ClientError {
        if (obj == null) {
            throw ClientError.ofNullReference(name);
        }

        return obj;
    }

    /**
     * <p>Creates a new instance of {@code ClientError} that describes a <i>propagated</i> error.
     *    <pre><code>
     *        var error = ClientError.ofThrowable(new IOException("oops"));
     *        throw err; // the stacktrace will display that it was caused by the IOException
     *    </code></pre>
     * </p>
     *
     * @param  t the {@link Throwable} that caused the error
     * @return   a {@code ClientError} that represents {@code t}
     * @author   Arsngrobg
     * @since    v0.0.2-pre-alpha
     */
    public static ClientError ofThrowable(Throwable t) {
        if (ClientError.requireNonNull(t, "t") instanceof ClientError asError) {
            return asError;
        }

        String message = String.format("caused by %s", t.getClass().getSimpleName());
        ClientError err = ClientError.with(Type.PROPAGATED, message);
        err.initCause(t);
        return err;
    }

    /**
     * <p>Creates a new instance of {@code ClientError} that describes a <i>null reference</i> for a particular
     *    object reference.
     *    <pre><code>
     *        var error = ClientError.ofNullReference("foo");
     *        throw error; // output: ClientError[Type: NULL_REFERENCE, Message: "foo" cannot be null.]
     *    </code></pre>
     * </p>
     * <p>If no {@code name} for the reference is provided, then the {@code message} of the new {@code ClientError}
     *    defaults to a basic one.
     * </p>
     *
     * @param  name the name of the object reference that caused this null reference error
     * @return      a new {@code ClientError} that describes the null reference
     * @author      Arsngrobg
     * @since       v0.0.2-pre-alpha
     */
    public static ClientError ofNullReference(String name) {
        if (name == null || name.isBlank()) {
            return ClientError.with(Type.NULL_REFERENCE, DEFAULT_NULL_REFERENCE_MESSAGE);
        }

        String message = String.format("\"%s\" cannot be null.", name);
        return ClientError.with(Type.NULL_REFERENCE, message);
    }

    /**
     * <p>Creates a new instance of a <i>generic</i> {@code ClientError} with the supplied {@code message}.
     *    <pre><code>
     *        var error = ClientError.withMessage("An error has occurred");
     *        throw error; // output: ClientError[Type: GENERIC, Message: An error has occurred]
     *    </code></pre>
     * </p>
     *
     * @param  message the {@code String} message to be displayed to be displayed upon encountering this
     *                 {@code ClientError}
     * @return         a new <i>generic</i> {@code ClientError} object with the supplied {@code message}
     * @author         Arsngrobg
     * @since          v0.0.2-pre-alpha
     */
    public static ClientError withMessage(String message) {
        return ClientError.with(Type.GENERIC, message);
    }

    /**
     * <p>Creates a new instance of {@code ClientError} with the supplied {@code type} ({@link ClientError.Type}) and
     *    {@code message}.
     *    <pre><code>
     *        var error = ClientError.with(ClientError.Type.GENERIC, "An error has occurred");
     *        throw error; // output: ClientError[Type: GENERIC, Message: An error has occurred]
     *    </code></pre>
     * </p>
     *
     * @param  type    the type of {@code ClientError} this is
     * @param  message the {@code String} message to be displayed to be displayed upon encountering this
     *                 {@code ClientError}
     * @return         a new {@code ClientError} object with the supplied {@code type} & {@code message}
     * @author         Arsngrobg
     * @since          v0.0.2-pre-alpha
     */
    public static ClientError with(Type type, String message) {
        return new ClientError(
                ClientError.requireNonNull(type,    "type"),
                ClientError.requireNonNull(message, "message")
        );
    }

    /**
     * <p>Discriminator enum type for {@code ClientError}.</p>
     * <p>It describes the cause of the error in the <b>SMPHook</b> system. For example, the {@code NULL_REFERENCE} case
     *    describes all illegal instances of {@code null}, these are always caught early as to not propagate the
     *    {@code null} through the system.
     * </p>
     *
     * @author Arsngrobg
     * @since  v0.0.2-pre-alpha
     */
    public enum Type {
        /** <p>Most basic form of a {@code ClientError}.</p> */
        GENERIC,
        /** <p>Caused by illegal {@code null} references <i>e.g. {@link NullPointerException}s</i>.</p> */
        NULL_REFERENCE,
        /** <p>Caused by other error types <i>(e.g. {@link java.io.IOException})</i>.</p> */
        PROPAGATED
    }

    private final Type type;
    private final String message;

    private ClientError(Type type, String message) {
        this.type    = type;
        this.message = message;
    }

    /**
     * <p>The type of {@code ClientError} this is.</p>
     * <p><i>See: {@link ClientError.Type}</i></p>
     *
     * @return the type of {@code ClientError} this is
     * @author Arsngrobg
     * @since  v0.0.2-pre-alpha
     */
    public Type getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof ClientError asError)) return false;
        return (
                type == asError.getType()            &&
                message.equals(asError.getMessage())
        );
    }

    @Override
    public String toString() {
        return String.format("ClientError[Type: %s, Message: %s]", getType(), getMessage());
    }
}