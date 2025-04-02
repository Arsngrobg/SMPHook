package dev.arsngrobg.smphook.events;

import static dev.arsngrobg.smphook.SMPHookError.condition;
import static dev.arsngrobg.smphook.SMPHookError.nullCondition;

import java.util.HashMap;
import java.util.Map;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;

/**
 * <p>The {@code StringMacro} class represents an identifier that is a textual substitution in a <b>prototype string</b>.
 * 
 * <p>The <b>prototype strings</b> are the portion of the server output that is displayed to the player(s) currently playing on the server.</p>
 * 
 * <p>For example, a {@link BaseEventType#PLAYER_MESSAGE} event can look like this:
 *    <blockquote><pre>
 *       [Arsngrobg] Hello, World!
 *    </pre></blockquote>
 *    and is represented as such in the <b>prototype string</b>:
 *    <blockquote><pre>
 *       [%username%] %message%
 *    </pre></blockquote>
 *    Where {@code %username%} & {@code %message%} are string macros
 *    Anything wrapped with {@code %} are considered string macros, which internally represent regex strings.
 *    If you want to use the {@code %} literal, use the string {@code %%} instead.
 * </p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class StringMacro {
    private static final Map<String, StringMacro> GLOBALS = new HashMap<>();

    /**
     * <p>Gets the defined {@code StringMacro} that has been defined by its unique {@code identifier}.</p>
     * 
     * @param identifier - the unique string that identifies a {@code StringMacro}
     * @return the {@code StringMacro} with the equivalent {@code identifier}
     * @throws SMPHookError if the {@code identifier} is {@code null} or if it has not been defined
     */
    public static StringMacro defined(String identifier) throws SMPHookError {
        SMPHookError.caseThrow(
            nullCondition(identifier, "identifier"),
            condition(() -> !GLOBALS.containsKey(identifier), SMPHookError.withMessage("No StringMacro with the identifier: %s is named.", identifier))
        );

        return GLOBALS.get(identifier);
    }

    /**
     * <p>Defines a new {@code StringMacro} with the supplied {@code identifier}, if there already is not a {@code StringMacro} with the {@code identifier} already defined.
     *    If there is already a {@code StringMacro} defined then this method will throw an {@link SMPHookError}.
     *    If not then you can get it by invoking the {@link StringMacro#defined(String)} method.
     * </p>
     * 
     * @param identifier - the identifier to uniquely identify this new {@code StringMacro}
     * @param regex - the regex value to be substituted in-place of the {@code identifier}
     * @throws SMPHookError if the {@code identifier} is {@code null} or has already been defined
     */
    public static void define(String identifier, String regex) throws SMPHookError {
        SMPHookError.caseThrow(
            nullCondition(identifier, "identifier"),
            condition(() -> GLOBALS.containsKey(identifier), SMPHookError.withMessage("StringMacro with the identifier: %s is already defined.", identifier))
        );

        StringMacro macro = new StringMacro(
            identifier,
            SMPHookError.strictlyRequireNonNull(regex, "regex")
        );

        GLOBALS.put(macro.identifier, macro);
    }

    private final String identifier;
    private final String regex;

    private StringMacro(String identifier, String regex) {
        this.identifier = identifier;
        this.regex      = regex;
    }

    /** @return the unique identifier of this {@code StringMacro} */
    public String getIdentifier() {
        return identifier;
    }

    /** @return the regex to insert in the place of this {@code StringMacro} */
    public String getRegex() {
        return regex;
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(identifier, regex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof StringMacro asMacro)) return false;
        return identifier.equals(asMacro.identifier);
    }

    @Override
    public String toString() {
        return String.format("#%s=%s", identifier, regex);
    }
}
