package dev.arsngrobg.smphook.events;

import dev.arsngrobg.smphook.SMPHookError;

/**
 * <p>The {@code EventType} interface represents an event type within the context of the SMPHook event capturing system.
 *    Each event has a number of positional argument types, where the types are defined by the use of the {@link TypeWrapper} functional interface.
 *    Each positional argument should be queried through the abstract methods provided through this interface and should not be publically accessible.
 * </p>
 * 
 * <p>The {@link BaseEventType} enum implement this interface.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    TypeWrapper
 * @see    BaseEventType
 */
public interface EventType {
    /**
     * <p>Gets the argument type at the {@code idx} of this {@code EventType}'s argument type list.
     *    The argument type is then casted to the {@code TypeWrapper<T>} - from the generic {@code TypeWrapper<?>}.
     * </p>
     * 
     * @param <T>   the bounded type of {@link TypeWrapper} to cast to
     * @param idx - the index in the argument type list (see the docs for information on the different event types)
     * @return      the {@link TypeWrapper} at the {@code idx} (if valid)
     * @throws SMPHookError if the {@code idx} is invalid or the generic {@link TypeWrapper} could not be casted as a bounded {@link TypeWrapper}
     */
    <T> TypeWrapper<T> getArgumentType(int idx) throws SMPHookError;

    /** @return the number of argument types this {@code EventType} has */
    int argCount();
}
