package dev.arsngrobg.smphook;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>The {@code SMPHookError} class represents an error in the <i>SMPHook</i> system.
 *    This class encapsulates various error types and provides factory methods to create errors.
 *    No external error types are used, only this error type.
 *    {@code SMPHookError}s can wrap pre-existing {@link java.lang.Throwable} types (errors / exceptions) through the {@link SMPHookError#withCause(Throwable)} factory method.
 * </p>
 * 
 * <p>The basic factory method to create any type of {@code SMPHookError} is through the {@link SMPHookError#with(ErrorType, String)} factory method.
 *    Every other factory method (<i>e.g. {@link SMPHookError#nullReference(String)}</i>) uses this base factory method to instantiate {@code SMPHookError}s.
 * </p>
 * 
 * <p>See {@link SMPHookError.ErrorType} for the various supported error types for an {@code SMPHookError}.</p>
 * 
 * <p>In all of the {@code SMPHookError} factory method signatures it declares that it also throws an {@code SMPHookError}.
 *    This is either due to the method inherintly throwing an {@code SMPHookError}, or for {@code null}-safety.
 * </p>
 * 
 * <p>It extends the {@link java.lang.Error} class, so it is said that any unusual or illegal state is considered un-recoverable.</p>
 * 
 * <p>This class also acts as an error handling class as it also provides methods for getting around erroneous states.
 *    For example: the {@link #ifFail(Test, Runnable)} or {@link #ifFail(SupplyingTest, Supplier)} methods use callback functions if the initial function fails,
 *    or for ignoring non-serious exceptions using {@link #consumeException(Test)}.
 *    These are implemented to remove ugly try-catch statements across the source code.
 * </p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    SMPHookError.ErrorType
 */
public final class SMPHookError extends Error {
    /** <p>Enumeration representing different types of error cases.</p> */
    public enum ErrorType {
        /** <p>The most basic error.</p> */
        GENERIC,
        /** <p>An error caused by file operations.</p> */
        FILE,
        /** <p>An error caused by IO operations.</p> */
        IO,
        /** <p>An error caused by concurrent operations.</p> */
        CONCURRENCY,
        /** <p>An error caused by an illegal {@code null} reference.</p> */
        NULL_REFERENCE,
        /** <p>An error that is caused by another {@link Throwable}.</p> */
        PROPAGATED
    }

    /** <p>A simple function to test a block of code for a thrown exception.</p> */
    @FunctionalInterface
    public interface Test {
        /**
         * <p>Executes the block of code wrapped by this function.</p>
         * 
         * @throws Exception any exception if the code fails
         */
        void test() throws Exception;
    }

    /**
     * <p>A simple function to test a block of code that can also return a value.</p>
     * 
     * @param <RETURN_TYPE> the return type of the test block
     */
    @FunctionalInterface
    public interface SupplyingTest<RETURN_TYPE> {
        /**
         * <p>Executes the block of code wrapped by this function and returns a value.</p>
         * 
         * @param <T> - the type to return from this test block
         * @return a value bound to the type {@code T}
         * @throws Exception any exception if the code fails
         */
        RETURN_TYPE test() throws Exception;
    }

    /**
     * <p>A function that handles an {@link Exception} thrown by a method or functions.</p>
     */
    @FunctionalInterface
    public interface ExceptionHandler {
        /**
         * <p>Handles the {@link Exception} thrown by a method or function</p>
         * 
         * @param e - the exception that was thrown
         */
        void handle(Exception e);
    }

    /** <p>A simple Data Transfer Object (DTO) for a conditional case used in {@link #caseThrow(ConditionalCase...)} </p> */
    public record ConditionalCase(SupplyingTest<Boolean> test, SMPHookError ifTrue) {}

    /**
     * <p>A {@link ConditionalCase} for {@code null} values.</p>
     * 
     * @param     obj - the {@code obj} to test for {@code null} reference
     * @param varName - the optional string of the variable name to output in the potential error message
     * @return a {@link ConditionalCase} Data Transfer Object (DTO)
     * @see #caseThrow(ConditionalCase...)
     */
    public static ConditionalCase nullCondition(Object obj, String varName) {
        return SMPHookError.condition(
            () -> obj == null,
            SMPHookError.nullReference(varName)
        );
    }

    /**
     * <p>Instantiates a simple Data Transfer Object (DTO) to be passed as an argument into the {@link #caseThrow(ConditionalCase...)} method.</p>
     * 
     * <p>This {@link ConditionalCase} DTO is designed to hold the condition in which it needs to pass or else the error {@code ifTrue} is thrown.</p>
     * 
     * @param   test - an executable block of code to test
     * @param ifTrue - the error to throw if the code block is {@code true}
     * @return a {@link ConditionalCase} DTO
     * @throws SMPHookError if {@code test} or {@code ifTrue} is {@code null}
     * @see #caseThrow(ConditionalCase...)
     */
    public static ConditionalCase condition(SupplyingTest<Boolean> test, SMPHookError ifTrue) throws SMPHookError {
        return new ConditionalCase(
            SMPHookError.strictlyRequireNonNull(test, "test"),
            SMPHookError.strictlyRequireNonNull(ifTrue, "ifTrue")
        );
    }

    /**
     * <p>Tests each condition supplied to this method in linear order.
     *    The first {@link ConditionalCase} that returns {@code true} throws its assigned {@code SMPHookError}.
     * </p>
     * 
     * <p>To easily define a {@link ConditionalCase} use the {@link #condition(SupplyingTest, SMPHookError)} factory method.</p>
     * 
     * @param cases - the cases to check for
     * @throws SMPHookError if any one of the conditions are {@code true}
     * @see #condition(SupplyingTest, SMPHookError)
     */
    public static void caseThrow(ConditionalCase... cases) throws SMPHookError {
        for (ConditionalCase c : cases) {
            SMPHookError.strictlyRequireNonNull(c, "ConditionalCase");
            
            boolean isTrue = false;
            try { isTrue = c.test.test(); } catch (Exception e) { throw SMPHookError.withCause(e); }
            if (isTrue) throw c.ifTrue;
        }
    }

    /**
     * <p>Tests the given executable block of code and consumes the generic {@link java.lang.Exception} if it was thrown.</p>
     * 
     * <p>The {@link java.lang.Exception} thrown from the code block is not handled, hence <i>consumed</i>.</p>
     * 
     * @param t - the executable block of code
     */
    public static void consumeException(Test t) {
        try { SMPHookError.strictlyRequireNonNull(t, "t").test(); }
        catch (Exception ignored) {}
    }

    /**
     * <p>Executes the test {@code t} and if it fails invokes its {@code callback} exception handler function.
     *    The callback function must not fail as this is the function which should gracefully exit out of an erroneous state.
     * </p>
     * 
     * @param        t - the function that may fail
     * @param callback - the safe {@link ExceptionHandler} function if {@code t} fails
     */
    public static void ifFailThen(Test t, ExceptionHandler callback) {
        try { SMPHookError.strictlyRequireNonNull(t, "t").test(); }
        catch (Exception e) { callback.handle(e); }
    }

    /**
     * <p>Executes the test {@code t} and returns the value from its result.
     *    If the test {@code t} fails (i.e. throws an exception or error) its callback function {@code ifFail} will execute.
     *    The {@code ifFail} callback is also a supplying function.
     * </p>
     * 
     * @param    <T>   the return type of both the supplying test {@code t} and the {@code ifFail} callback
     * @param      t - the initial test to perform (returning the type {@code T})
     * @param ifFail - the callback function to execute if {@code t} fails (also returning the type {@code T})
     * @return the value returned from either the {@code t} supplying test or the {@code ifFail} callback
     */
    public static <T> T ifFail(SupplyingTest<T> t, Supplier<T> ifFail) {
        try { return SMPHookError.strictlyRequireNonNull(t, "t").test(); }
        catch (Exception | Error ignored) { return SMPHookError.throwIfFail(ifFail::get); }
    }

    /**
     * <p>Executes the test {@code t} and if it throws an {@link Exception} or {@link Error} then it will invoke the {@code ifFail} callback.
     *    Make sure your {@code ifFail} callback does not fail as this can cause unintended behaviour.
     * </p>
     * 
     * @param      t - the function that may fail
     * @param ifFail - the function to invoke upon failure of the {@code t} function
     */
    public static void ifFail(Test t, Runnable ifFail) {
        try { SMPHookError.strictlyRequireNonNull(t, "t").test(); }
        catch (Exception | Error ignored) { SMPHookError.throwIfFail(ifFail::run); }
    }

    /**
     * <p>Executes the supplied test block {@code t}, and returns the value of type {@code RETURN_TYPE}.</p>
     * 
     * <p>A <b>propagated</b> {@code SMPHookError} if the test block throws an exception,
     *    or a <b>NULL_REFERENCE</b> {@code SMPHookError} if {@code t} is {@code null}.
     * </p>
     * 
     * @param <RETURN_TYPE>   the return type of the executable code block
     * @param             t - the executable code block to test for an exception
     * @return a value of type {@code RETURN_TYPE}
     * @throws SMPHookError if the {@code t} is {@code null} or the code block throws an exception
     */
    public static <RETURN_TYPE> RETURN_TYPE throwIfFail(SupplyingTest<RETURN_TYPE> t) throws SMPHookError {
        SMPHookError.strictlyRequireNonNull(t, "t");

        try { return t.test(); } catch (Exception e) { throw SMPHookError.withCause(e); }
    }

    /**
     * <p>Executes the supplied test block {@code t}.</p>
     * 
     * <p>A <b>propagated</b> {@code SMPHookError} if the test block throws an exception,
     *    or a <b>NULL_REFERENCE</b> {@code SMPHookError} if {@code t} is {@code null}.
     * </p>
     * 
     * @param t - the executable code block to test for an exception
     * @throws SMPHookError if the {@code t} is {@code null} or the code block throws an exception
     */
    public static void throwIfFail(Test t) throws SMPHookError {
        try { SMPHookError.strictlyRequireNonNull(t, "t").test(); }
        catch (Exception e) { throw SMPHookError.withCause(e); }
    }

    /**
     * <p>This is an alternative to {@link SMPHookError#strictlyRequireNonNull(Object, String)}.</p>
     * 
     * <p>This method checks for {@code null} safety with the value of {@code obj}.
     *    If {@code obj} is {@code null} then the {@code alt}ernative object is returned.
     *    This method throws an {@code SMPHookError} if the {@code alt} argument is {@code null}.
     * </p>
     * 
     * @param <T>   the type of object to check for {@code null} saftey, and subsequently return if {@code null}
     * @param obj - the object to check for {@code null} saftey
     * @param alt - the object to return if {@code obj} is null
     * @return returns the {@code obj} is not {@code null} or {@code alt} if otherwise
     * @throws SMPHookError if {@code alt} is {@code null} when {@code obj} is also {@code null}
     */
    public static <T> T requireNonNull(T obj, T alt) throws SMPHookError {
        if (obj == null) {
            return SMPHookError.strictlyRequireNonNull(alt, "alt");
        }

        return obj;
    }

    /**
     * <p>This method checks for {@code null} safety for each element of the supplied {@code arr}.
     *    If any element in the {@code arr} is {@code null} then a <b>NULL_REFERENCE</b> {@code SMPHookError} is thrown.
     *    The {@code arrName} is an optional string value to provide better error messages.
     * </p>
     * 
     * @param <T> the type of the array to check for null safety
     * @param arr - the array to check for {@code null} values
     * @param arrName - the optional string value to provide to the error if required by this method
     * @return the {@code arr} argument
     * @throws SMPHookError a <b>NULL_REFERENCE</b> {@code SMPHookError} if any element of {@code arr} is {@code null}
     */
    public static <T> T[] strictlyRequireNonNull(T[] arr, String arrName) throws SMPHookError {
        for (int idx = 0; idx < arr.length; idx++) {
            T element = arr[idx];
            if (element != null) continue;

            String errMsg;
            if (arrName == null) errMsg = String.format("Array element [%d]", idx);
            else                 errMsg = String.format("%s[%d]", arrName, idx);
            throw SMPHookError.nullReference(errMsg);
        }

        return arr;
    }

    /**
     * <p>This is a strict {@code null} safety validator method.</p>
     * 
     * <p>If the {@code obj} argument is {@code null} then this method will throw a <b>NULL_REFERENCE</b> {@code SMPHookError}.
     *    The {@code varName} is an optional string value that can provide better error messaging.
     * </p>
     * 
     * @param     <T>   the type of object to check for {@code null} safety
     * @param     obj - the the object to check for {@code null} safety
     * @param varName - the optional string value to provide to the error if required by this method
     * @return the {@code obj} argument
     * @throws SMPHookError a <b>NULL_REFERENCE</b> {@code SMPHookError} if {@code obj} is {@code null}
     */
    public static <T> T strictlyRequireNonNull(T obj, String varName) throws SMPHookError {
        if (obj == null) {
            throw SMPHookError.nullReference(varName);
        }

        return obj;
    }

    /**
     * <p>Instantiates a new {@code SMPHookError} instance of the type <b>NULL_REFERENCE</b>,
     *    with the supplied {@code varName} to notify the user of a {@code null} reference being used when not allowed.
     * </p>
     * 
     * <p>If the supplied {@code varName} is {@code null} then the message will use a default message.</p>
     * 
     * @param varName - the variable name to put in the custom error string
     * @return a new <b>NULL_REFERENCE</b> {@code SMPHookError} instance
     */
    public static SMPHookError nullReference(String varName) {
        if (varName == null || varName.replaceAll("\\s+", "").isEmpty()) {
            return SMPHookError.with(ErrorType.NULL_REFERENCE, "Illegal null reference.");
        }

        String errMsg = String.format("'%s' cannot be null.", varName);
        return SMPHookError.with(ErrorType.NULL_REFERENCE, errMsg);
    }

    /**
     * <p>Instantiates a <b>PROPAGATED</b> {@code SMPHookError} instance with a detailed error message,
     *    including the {@code .java} file and line number this occurred in and the message (if provided).
     * </p>
     * 
     * @param t - the {@link Throwable}
     * @return a new <b>PROPAGATED</b> {@code SMPHookError} instance
     * @throws SMPHookError if {@code t} is {@code null}
     */
    public static SMPHookError withCause(Throwable t) throws SMPHookError {
        SMPHookError.strictlyRequireNonNull(t, "t");

        StackTraceElement[] stacktrace = t.getStackTrace();
        if (stacktrace.length == 0) {
            return SMPHookError.with(ErrorType.PROPAGATED, String.format("Thrown by %s.", t.getClass().getSimpleName()));
        }

        StringBuilder sb = new StringBuilder(t.getClass().getSimpleName()).append(" was thrown at ");

        for (StackTraceElement stacktraceElement : stacktrace) {
            sb.append(stacktraceElement.getFileName(), 0, stacktraceElement.getFileName().length() - 4)
              .append(stacktraceElement.getMethodName())
              .append("(")
              .append(stacktraceElement.getLineNumber())
              .append(") -> ");
        }

        if (t.getMessage() != null) {
            sb.append("\"").append(t.getMessage()).append("\"");
        }

        return SMPHookError.with(ErrorType.PROPAGATED, sb.toString());
    }

    /**
     * <p>Instantiates a new {@code SMPHookError} instance of the type <b>GENERIC</b> with the supplied {@code format} and {@code args}</p>
     * 
     * @param format - the format string for the message
     * @param   args - the argumets to format in place of the message
     * @return a new <b>GENERIC</b> {@code SMPHookError} instance
     */
    public static SMPHookError withMessage(String format, Object... args) {
        return SMPHookError.withMessage(String.format(format, args));
    }

    /**
     * <p>Instantiates a new {@code SMPHookError} instance of the type <b>GENERIC</b> with the supplied {@code message}.</p>
     * 
     * @param message - the message to supply the the new {@code SMPHookError}
     * @return a new <b>GENERIC</b> {@code SMPHookError} instance
     */
    public static SMPHookError withMessage(String message) {
        return SMPHookError.with(ErrorType.GENERIC, message);
    }

    /**
     * <p>Instantiates a new {@code SMPHookError} instance with the specified {@code type} and the supplied {@code message}.</p>
     * 
     * <p>If no {@code type} is given, it defaults to a <b>GENERIC</b> type.</p>
     * <p>If no {@code message} is given, it defaults to an empty string.</p>
     * 
     * @param    type - the type of error to instantiate
     * @param message - the string message to supply to the new {@code SMPHookError}
     * @return a new {@code SMPHookError} instance of {@code type}
     */
    public static SMPHookError with(ErrorType type, String message) {
        return new SMPHookError(
            SMPHookError.requireNonNull(type, ErrorType.GENERIC),
            SMPHookError.requireNonNull(message, "")
        );
    }

    private final ErrorType type;
    private final String    message;

    private SMPHookError(ErrorType type, String message) {
        this.type    = type;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /** @return the type of error this instance of {@code SMPHookError} is */
    public ErrorType getType() {
        return type;
    }
    
    @Override
    public int hashCode() {
        return SMPHook.hashOf(type, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SMPHookError asError)) return false;
        return type == asError.type && message.equals(asError.message);
    }

    @Override
    public String toString() {
        return String.format("SMPHookError[type: %s, message: \"%s\"]", getType(), getMessage());
    }
}
