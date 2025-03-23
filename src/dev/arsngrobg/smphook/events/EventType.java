package dev.arsngrobg.smphook.events;

import dev.arsngrobg.smphook.SMPHookError;
import static dev.arsngrobg.smphook.events.TypeWrapper.*;

public enum EventType {
    /**
     * <p>Invoked when the server has fully initialised.</p>
     * <p>Positional arguments:
     *    <ol>
     *       <li>init_time: {@code float} | the time it took for the server to initialise</li>
     *    </ol>
     * </p>
     * 
     */
    @EquivalentServerOutput("Done (<init_time>s)! For help, type \"help\"")
    SERVER_READY(f32);

    private final TypeWrapper<?>[] argumentTypes;

    EventType(TypeWrapper<?>... argumentTypes) {
        this.argumentTypes = argumentTypes;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeWrapper<T> getArgumentType(int idx) throws SMPHookError {
        return (TypeWrapper<T>) SMPHookError.throwIfFail(() -> argumentTypes[idx]);
    }

    public TypeWrapper<?>[] getArgumentTypes() {
        return argumentTypes;
    }
}
