package dev.arsngrobg.smphook.events;

import dev.arsngrobg.smphook.SMPHookError;
import static dev.arsngrobg.smphook.events.TypeWrapper.*;

/**
 * <p>The {@code BaseEventType} enum consists of the default event types captured from vanilla Minecraft servers.
 *    It implements the {@link EventType} interface and each <b>default</b> event type consists of their positional argument types.
 *    Each {@code BaseEventType} are annotated with their generic vanilla server output messages, and can be overriden using the {@link CustomEventType} class. 
 * </p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    EventType
 * @see    EquivalentServerOutput
 */
public enum BaseEventType implements EventType {
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

    @EquivalentServerOutput   ("[%username%] %message%")
    PLAYER_MESSAGE            (string, string),

    @EquivalentServerOutput   ("%username% moved too quickly! %x%,%y%,%z%")
    PLAYER_MOVED_TOO_QUICKLY  (string, f32, f32, f32),

    @EquivalentServerOutput   ("%vehicle% (vehicle of %username%) moved too quickly! %x%,%y%,%z%")
    VEHICLE_MOVED_TOO_QUICKLY (string, string, f32, f32, f32);

    private final TypeWrapper<?>[] argumentTypes;

    BaseEventType(TypeWrapper<?>... argumentTypes) {
        this.argumentTypes = argumentTypes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeWrapper<T> getArgumentType(int idx) throws SMPHookError {
        return SMPHookError.throwIfFail(() -> (TypeWrapper<T>) argumentTypes[idx]);
    }

    @Override
    public int argCount() {
        return argumentTypes.length;
    }
}
