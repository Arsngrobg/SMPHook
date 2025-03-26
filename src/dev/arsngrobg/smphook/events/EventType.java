package dev.arsngrobg.smphook.events;

import dev.arsngrobg.smphook.SMPHookError;
import static dev.arsngrobg.smphook.events.TypeWrapper.*;

public enum EventType {
    @EquivalentServerOutput   ("Done (%init_time%s)! For help, type \"help\"")
    SERVER_READY              (f32),

    @EquivalentServerOutput   ("ThreadedAnvilChunkStorage: All dimensions are saved")
    SERVER_STOPPED            (),

    @EquivalentServerOutput   ("Server empty for %n% seconds, pausing")
    SERVER_PAUSED             (i32),

    @EquivalentServerOutput   ("Can't keep up! Is the server overloaded? Running %millis%ms or %ticks% ticks behind")
    SERVER_OVERLOADED         (i64, i64),

    @EquivalentServerOutput   ("[Not Secure] [Server] %message%")
    SERVER_MESSAGE            (string),

    @EquivalentServerOutput   ("UUID of player %username% is %uuid%")
    PLAYER_AUTHENTICATING     (string, string),

    @EquivalentServerOutput   ("%username%[/%ip%:%port%] logged in with entity id %entity_id% at (%x%, %y%, %z%)")
    PLAYER_JOINED             (string, string, i16, i32, f32, f32, f32),

    @EquivalentServerOutput   ("%username% lost connection: %reason%")
    PLAYER_LEAVE              (string, string),

    @EquivalentServerOutput   ("Banned %username%: %reason%")
    PLAYER_BANNED             (string, string),

    @EquivalentServerOutput   ("%username% moved too quickly! %x%,%y%,%z%")
    PLAYER_MOVED_TOO_QUICKLY  (string, f32, f32, f32),

    @EquivalentServerOutput   ("%vehicle% (vehicle of %username%) moved too quickly! %x%,%y%,%z%")
    VEHICLE_MOVED_TOO_QUICKLY (string, string, f32, f32, f32);

    private final TypeWrapper<?>[] argumentTypes;

    EventType(TypeWrapper<?>... argumentTypes) {
        this.argumentTypes = argumentTypes;
    }

    /**
     * <p>Gets the argument type at the {@code idx} of this {@code EventType}'s argument type list.
     *    The argument type is then casted to the {@code TypeWrapper<T>} - from the generic {@code TypeWrapper<?>}.
     * </p>
     * 
     * @param <T>   the bounded type of {@link TypeWrapper} to cast to
     * @param idx - the index in the argument type list (see the docs for information on the different event types)
     * @return the {@link TypeWrapper} at the {@code idx} (if valid)
     * @throws SMPHookError if the {@code idx} is invalid or the generic {@link TypeWrapper} could not be casted as a bounded {@link TypeWrapper}
     */
    @SuppressWarnings("unchecked")
    public <T> TypeWrapper<T> getArgumentType(int idx) throws SMPHookError {
        return SMPHookError.throwIfFail(() -> (TypeWrapper<T>) argumentTypes[idx]);
    }

    /** @return the number of argument types this {@code EventType} has */
    public int argCount() {
        return argumentTypes.length;
    }
}
