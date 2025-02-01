package dev.arsngrobg.smphook;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * <p>The {@code SMPHookError} class represents an error in the SMPHook system.
 *    This class encapsulates various error types and provides factory methods to create errors, including <b>GENERIC</b>, <b>NULL_REFERENCE</b>, and <b>PROPOGATED</b> types.
 * </p>
 * 
 * <p>See {@link SMPHookError.ErrorType} for the various supported error types for an {@code SMPHookError}.</p>
 * 
 * <p>It extends the {@link java.lang.Error} class, so it is said that any unusual or illegal state is considered un-recoverable.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class SMPHookError extends Error {
    /** <p>A simple function to test a block of code.</p> */
    @FunctionalInterface
    public static interface Test {
        public void test() throws Exception;
    }

    /** <p>A function that executes the block of code, and returns a value of type {@code B} if success.</p> */
    @FunctionalInterface
    public static interface SupplyingTest<RETURN_TYPE> {
        public RETURN_TYPE test() throws Exception;
    }

    /** <p>Enumeration representing different types of errors.</p> */
    public static enum ErrorType {
        /** <p>The most basic error.</p> */
        GENERIC,
        /** <p>An error caused by failed file operations.</p> */
        FILE,
        /** <p>An error caused by failed I/O operations.</p> */
        IO,
        /** <p>An error caused by failed concurrency operations.</p> */
        CONCURRENCY,
        /** <p>An error caused by a null reference.</p> */
        NULL_REFERENCE,
        /** <p>An error caused by the internal type wrapped by a wrapper type.</p> */
        WRAPPER_TYPE,
        /** <p>An error that is propogated by another throwable.</p> */
        PROPAGATED
    }

    /** <p>A simple Data Tranfer Object (DTO) for a condition case used in {@link #caseThrow(Case...)}</p> */
    public record Case(Supplier<Boolean> condition, SMPHookError ifTrue) {}

    /**
     * <p>A {@link #caseThrow(Case...)} case for {@code null} values.</p>
     * 
     * @param o - the object to test
     * @param identifier - the identifier name to output if an error occurs
     * @return a {@link Case} DTO
     * @see #caseThrow(Case...)
     */
    public static SMPHookError.Case nullCase(Object o, String identifier) {
        return condition(() -> o == null, SMPHookError.nullReference(identifier));
    }

    /**
     * <p>Constructs a Data Transfer Object (DTO) to be passed into the {@link #caseThrow(Case...)} method.</p>
     * 
     * <p>This {@link Case} DTO is designed to hold the condition in which needs to pass or else the error ({@code ifTrue}) is thrown.</p>
     * 
     * @param condition - a function to test
     * @param ifTrue - the error thrown if the {@code condition} test is {@code true}
     * @return a {@link Case} DTO
     * @throws SMPHookError if {@code condition} or {@code ifTrue} is {@code null}
     * @see #caseThrow(Case...)
     */
    public static SMPHookError.Case condition(Supplier<Boolean> condition, SMPHookError ifTrue) throws SMPHookError {
        return new Case(
            SMPHookError.requireNonNull(condition),
            SMPHookError.requireNonNull(ifTrue)
        );
    }

    /**
     * <p>Tests each case condition supplied to this method in order.
     *    The first condition that returns {@code true} throws the assigned {@code SMPHookError}.
     * </p>
     * 
     * <p>To easily define a condition, use the {@link #condition(Supplier, SMPHookError)} method.</p>
     * 
     * @param cases - the cases to check for
     * @throws SMPHookError
     * @see SMPHookError#condition(Supplier, SMPHookError)
     */
    @SafeVarargs
    public static void caseThrow(SMPHookError.Case... cases) throws SMPHookError {
        for (Case c : cases) {
            SMPHookError.requireNonNull(c);
            boolean isTrue = c.condition.get();
            if (isTrue) throw c.ifTrue;
        }
    }

    /**
     * <p>Executes the supplied test ({@code t} code block.</p>
     * 
     * <p>Upon a thrown {@link Exception}, it will be consumed (ignored).</p>
     * 
     * @param t - the test to execute
     */
    public static void consumeException(Test t) {
        SMPHookError.requireNonNull(t);

        try { t.test(); } catch (Exception ignored) {}
    }

    /**
     * <p>Executes the supplied test ({@code t}) code block.</p>
     * 
     * <p>A <b>PROPOGATED</b> {@code SMPHookError} is thrown if this test fails.</p>
     * 
     * @param t - the test to check for thrown exceptions
     * @throws SMPHookError if an exception was thrown by the test ({@code t})
     */
    public static void throwIfFail(Test t) throws SMPHookError {
        SMPHookError.requireNonNull(t);

        try { t.test(); } catch (Exception e) { throw SMPHookError.withCause(e); }
    }

    /**
     * <p>Executes the supplied test ({@code t}) code block, and returns the value of type {@code RETURN_TYPE}.</p>
     * 
     * <p>A <b>PROPOGATED</b> {@code SMPHookError} is thrown if this test fails.</p>
     * 
     * @param <RETURN_TYPE> - the type that the test ({@code t}) returns
     * @param t - the test to check for thrown exceptions
     * @throws SMPHookError if an exception was thrown by the test ({@code t})
     */
    public static <RETURN_TYPE> RETURN_TYPE throwIfFail(SupplyingTest<RETURN_TYPE> t) throws SMPHookError {
        SMPHookError.requireNonNull(t);

        try { return t.test(); } catch (Exception e) { throw SMPHookError.withCause(e); }
    }

    /**
     * <p>Instantiates a {@link ErrorType#PROPAGATED} {@code SMPHookError} instance with a detailed error message,
     *    including the {@code .java} file and line number this occurred in and the message (if provided).
     * </p>
     * 
     * @param t - the {@link java.lang.Throwable}
     * @return a new <b>PROPOGATED</b> {@code SMPHookError} instance
     * @throws SMPHookError if t is {@code null}
     */
    public static SMPHookError withCause(Throwable t) throws SMPHookError {
        SMPHookError.requireNonNull(t);

        StringBuilder sb = new StringBuilder();

        StackTraceElement first = t.getStackTrace()[0];
        sb.append(t.getClass().getName())
          .append(" was thrown at ")
          .append(first.getFileName().substring(0, first.getFileName().length() - 4))
          .append(first.getMethodName())
          .append("(")
          .append(first.getLineNumber())
          .append(") - \"")
          .append(t.getMessage())
          .append("\"");

        return SMPHookError.with(ErrorType.PROPAGATED, sb.toString());
    }

    /**
     * <p>Checks to see if the {@code obj} is {@code null}.
     *    If that is the case, then the {@code alt} is returned.
     * </p>
     * 
     * @param <T> - the type of object that is passed to this method
     * @param obj - the object to check for {@code null} safety
     * @param alt - the object to return if {@code obj} is {@code null}
     * @return the {@code obj} or {@code alt}
     */
    public static <T> T requireNonNull(T obj, T alt) {
        if (obj == null) {
            return SMPHookError.requireNonNull(alt);
        }

        return SMPHookError.requireNonNull(obj);
    }

    /**
     * <p>Checks to see if the {@code obj} is {@code null}.
     *    If that is the case, then an {@code SMPHookError} is thrown.
     * </p>
     * 
     * @param <T> - the type of object that is passed to this method
     * @param obj - the object to check for {@code null} safety
     * @return the {@code obj} for optional chaining
     * @throws SMPHookError if the {@code obj} is {@code null}
     */
    public static <T> T requireNonNull(T obj) throws SMPHookError {
        if (obj == null) {
            throw SMPHookError.nullReference("obj");
        }

        return obj;
    }

    /**
     * <p>Instantiates a {@link ErrorType#NULL_REFERENCE} {@code SMPHookError} instance with a detailed error message using the supplied {@code identifiers}.</p>
     * 
     * <p>If {@code identifiers} is empty, a simple error message is used.</p>
     * 
     * @param identifiers - the list of variables to display in the detailed error message
     * @return a new <b>NULL_REFERENCE</b> {@code SMPHookError} instance
     */
    @SafeVarargs
    public static SMPHookError nullReference(String... identifiers) {
        String errMsg;

        if (identifiers.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String identifier : identifiers) {
                sb.append("'").append(SMPHookError.requireNonNull(identifier)).append("', ");
            }
            sb.setLength(sb.length() - 2);
            sb.append(" cannot be null.");
            errMsg = sb.toString();
        } else errMsg = "Null references disallowed.";

        return SMPHookError.with(ErrorType.NULL_REFERENCE, errMsg);
    }

    /**
     * <p>Instantiates a {@link ErrorType#GENERIC} {@code SMPHookError} instance with the supplied {@code message}.</p>
     * 
     * <p>If {@code message} is {@code null}, then an empty string is used.</p>
     * 
     * @param message - the string message to supply to this <b>GENERIC</b> error.
     * @return a new <b>GENERIC</b> {@code SMPHookError} instance
     */
    public static SMPHookError withMessage(String message) {
        return SMPHookError.with(ErrorType.GENERIC, message);
    }

    /**
     * <p>Instantiates an instance of {@code SMPHookError} with the supplied error {@code type} and {@code message}.</p>
     * 
     * <p>If {@code type} is {@code null}, then the default {@link ErrorType#GENERIC} is used.</p>
     * <p>If {@code message} is {@code null}, then an empty string is used./p>
     * 
     * @param type - the error type of this new {@code SMPHookError} instance
     * @param message - the string message to supply to this new {@code SMPHookError} instance
     * @return a new {@code SMPHookError} instance
     */
    public static SMPHookError with(ErrorType type, String message) {
        return new SMPHookError(type, message);
    }

    private final ErrorType type;
    private final String message;

    private SMPHookError(ErrorType type, String message) {
        this.type    = SMPHookError.requireNonNull(type, ErrorType.GENERIC);
        this.message = SMPHookError.requireNonNull(message, "");
    }

    /** @return the type of {@code SMPHookError} this is */
    public ErrorType getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return String.format("(%s): %s", type, message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)                  return false;
        if (obj == this)                  return true;
        if (getClass() != obj.getClass()) return false;
        SMPHookError asError = (SMPHookError) obj;
        return type == asError.type && message.equals(asError.message);
    }

    @Override
    public String toString() {
        return String.format("SMPHookError[type: %s, message: \"%s\"]", type, message);
    }
}
