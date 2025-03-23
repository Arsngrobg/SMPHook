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
     */
    @EquivalentServerOutput("Done (%init_time%s)! For help, type \"help\"")
    SERVER_READY      (f32),

    /** <p>Invoked when the server has stopped running.</p> */
    @EquivalentServerOutput("ThreadedAnvilChunkStorage: All dimensions are saved")
    SERVER_STOPPED    (),

    /**
     * <p>Invoked when the server has paused.</p>
     * <p>Positional arguments:
     *    <ol>
     *       <li>n: {@code int} | the amount of time (in seconds) the server was empty for before pausing</li>
     *    </ol>
     * </p>
     */
    @EquivalentServerOutput("Server empty for %n% seconds, pausing")
    SERVER_PAUSED     (i32),

    /**
     * <p>Invoked when the server is overloaded.</p>
     * <p>Positional arguments:
     *    <ol>
     *       <li>millis: {@code long} | the milliseconds the server is behind</li>
     *       <li>ticks: {@code long} | the number of ticks the server could not process</li>
     *    </ol>
     * </p>
     */
    @EquivalentServerOutput("Can't keep up! Is the server overloaded? Running %millis%ms or %ticks% ticks behind")
    SERVER_OVERLOADED (i64, i64);

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
