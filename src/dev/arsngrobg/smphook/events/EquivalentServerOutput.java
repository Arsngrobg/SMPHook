package dev.arsngrobg.smphook.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>The {@code EquivalentServerOutput} annotation includes metadata for an {@link EventType} enum value.
 *    This metadata is the <b>prototype string</b> for the specific {@link EventType} enum value.
 * </p>
 * 
 * <p>The <b>prototype strings</b> are the portion of the server output that is displayed to the player(s) currently playing on the server.</p>
 * 
 * <p>For example, a {@link EventType#PLAYER_MESSAGE} event can look like this:
 *    <blockquote><pre>
 *       [Arsngrobg] Hello, World!
 *    </pre></blockquote>
 *    and is represented as such in the <b>prototype string</b>:
 *    <blockquote><pre>
 *       [%username%] %message%
 *    </pre></blockquote>
 *    Where {@code %username%} & {@code %message%} are specific regex strings from a table of known regex strings (macros).
 * </p>
 * 
 * <p>These <b>prototype strings</b> can be read during the runtime of SMPHook, and is used to catch events from the server.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EquivalentServerOutput {
    /** <p>The <b>prototype string</b></p> */
    String value();
}
