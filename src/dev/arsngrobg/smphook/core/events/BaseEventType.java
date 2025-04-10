package dev.arsngrobg.smphook.core.events;

import dev.arsngrobg.smphook.SMPHookError;
import static dev.arsngrobg.smphook.core.events.TypeWrapper.*;

/**
 * <p>The {@code BaseEventType} enum consists of the default event types captured from vanilla Minecraft servers.
 *    It implements the {@link EventType} interface and each <b>default</b> event type consists of their positional argument types.
 * </p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    EventType
 * @see    EquivalentServerOutput
 */
public enum BaseEventType implements EventType {
    @EquivalentServerOutput   ("Done (%init_time%s)! For help, type \"help\"")
    SERVER_READY              (number),

    @EquivalentServerOutput   ("ThreadedAnvilChunkStorage: All dimensions are saved")
    SERVER_STOPPED            (),

    @EquivalentServerOutput   ("Server empty for %n% seconds, pausing")
    SERVER_PAUSED             (number),

    @EquivalentServerOutput   ("Can't keep up! Is the server overloaded? Running %millis%ms or %ticks% ticks behind")
    SERVER_OVERLOADED         (number, number),

    @EquivalentServerOutput   ("[Not Secure] [Server] %message%")
    SERVER_MESSAGE            (string),

    @EquivalentServerOutput   ("UUID of player %username% is %uuid%")
    PLAYER_AUTHENTICATING     (string, string),

    @EquivalentServerOutput   ("%username%[/%ip%:%port%] logged in with entity id %entity_id% at (%x%, %y%, %z%)")
    PLAYER_JOINED             (string, string, number, number, number, number, number),

    @EquivalentServerOutput   ("%username% lost connection: %reason%")
    PLAYER_LEAVE              (string, string),

    @EquivalentServerOutput   ("Banned %username%: %reason%")
    PLAYER_BANNED             (string, string),

    @EquivalentServerOutput   ("[%username%] %message%")
    PLAYER_MESSAGE            (string, string),

    @EquivalentServerOutput   ("%username% moved too quickly! %x%,%y%,%z%")
    PLAYER_MOVED_TOO_QUICKLY  (string, number, number, number),

    @EquivalentServerOutput   ("%vehicle% (vehicle of %username%) moved too quickly! %x%,%y%,%z%")
    VEHICLE_MOVED_TOO_QUICKLY (string, string, number, number, number);

    // macro definitions
    static {
        StringMacro.define("init_time", "\\d+\\.\\d{3}");
        StringMacro.define("n",         "\\d+");
        StringMacro.define("millis",    "\\d+");
        StringMacro.define("ticks",     "\\d+");
        StringMacro.define("message",   "\\.");
        StringMacro.define("username",  "[A-Za-z0-9]{3,16}");
        StringMacro.define("uuid",      "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
        StringMacro.define("ip",        "TODO"); // TODO: IP capture
        StringMacro.define("port",      "\\d+");
        StringMacro.define("entity_id", "\\d+");
        StringMacro.define("x",         "\\d+\\.\\d+");
        StringMacro.define("y",         "\\d+\\.\\d+");
        StringMacro.define("z",         "\\d+\\.\\d+");
        StringMacro.define("vehicle",   "\\.{50}");
    }

    private final TypeWrapper<?>[] args;

    BaseEventType(TypeWrapper<?>... args) {
        this.args = args;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeWrapper<T> getArgumentType(int idx) throws SMPHookError {
        return SMPHookError.throwIfFail(() -> (TypeWrapper<T>) args[idx]);
    }

    @Override
    public int argCount() {
        return args.length;
    }
}
