package dev.arsngrobg.smphook.events;

import java.util.HashMap;
import java.util.Map;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;

/**
 * <p>The {@code StringMacro} class represents a globally defined macro that stores a string value in the form of a regular expression (regex).</p>
 * 
 * <p>This class provides static methods to define and retrieve global {@code StringMacro}s by name. It also ensures that no macros with the same name can be defined multiple times.</p>
 * 
 * <p>Each {@code StringMacro} is uniquely identified by its {@code name} and has a corresponding {@code regex} string value.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    EquivalentServerOutput
 * @see    EventType
 */
public final class StringMacro {
    // predefined macros
    static {
        define("username", "[A-Za-z0-9_]{3,16}");
    }

    private static final Map<String, StringMacro> GLOBALS = new HashMap<>();

    /**
     * <p>Retreives the globally-defined {@code StringMacro} object that has the {@code name}.</p>
     * 
     * @param name - the identifier of the already defined {@code StringMacro}
     * @return the globally defined {@code StringMacro} that has the {@code name}
     * @throws SMPHookError if the {@code StringMacro} is not defined
     */
    public static StringMacro defined(String name) throws SMPHookError {
        if (!GLOBALS.containsKey(name)) {
            throw SMPHookError.withMessage(String.format("StringMacro[%s] is not defined!", name));
        }

        return GLOBALS.get(name);
    }

    /**
     * <p>Defines a new {@code StringMacro} with the supplied {@code name} and value ({@code regex}).</p>
     * 
     * @param name - the identifier of the new macro
     * @param regex - the value of the new macro
     * @return a new {@code StringMacro} object
     * @throws SMPHookError if {@code name} or {@code regex} are {@code null}, or the {@code StringMacro} is already defined
     */
    public static StringMacro define(String name, String regex) throws SMPHookError {
        if (GLOBALS.containsKey(name)) {
            throw SMPHookError.withMessage(String.format("StringMacro[%s] is already defined!", name));
        }

        StringMacro macro = new StringMacro(
            SMPHookError.strictlyRequireNonNull(name, "name"),
            SMPHookError.strictlyRequireNonNull(regex, "regex")
        );
        
        GLOBALS.put(macro.name, macro);
        return macro;
    }

    private final String name;
    private final String regex;

    private StringMacro(String name, String regex) {
        this.name  = name;
        this.regex = regex;
    }

    /** @return the name of this {@code StringMacro} */
    public String getName() {
        return name;
    }

    /** @return the value of this {@code StringMacro}, which is the regex replacement */
    public String getRegex() {
        return regex;
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(name, regex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof StringMacro asMacro)) return false;
        return name.equals(asMacro.name);
    }

    @Override
    public String toString() {
        return String.format("\\%%s\\%='%s'", name, regex);
    }
}
