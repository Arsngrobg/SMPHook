package dev.arsngrobg.smphook.events;

import java.util.HashMap;
import java.util.Map;

import dev.arsngrobg.smphook.SMPHookError;

public final class StringMacro {
    private static final Map<String, StringMacro> globals = new HashMap<>();

    static {
        StringMacro[] predefined = {
            define("username", "")
        };
        // ... add to globals
    }

    public static StringMacro defined(String identifier) throws SMPHookError {
        if (!globals.containsKey(identifier)) {
            throw SMPHookError.withMessage(String.format("StringMacro[%s] has not been defined!", identifier));
        }

        return globals.get(identifier);
    }

    private static StringMacro define(String identifier, String regex) throws SMPHookError {
        if (globals.containsKey(identifier)) {
            throw SMPHookError.withMessage(String.format("StringMacro[%s] is already defined!", identifier));
        }

        return new StringMacro(
            SMPHookError.strictlyRequireNonNull(identifier, "identifier"),
            SMPHookError.strictlyRequireNonNull(regex, "regex")
        );
    }

    private final String identifier;
    private final String regex;

    private StringMacro(String identifier, String regex) {
        this.identifier = identifier;
        this.regex      = regex;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String asRegex() {
        return regex;
    }


}
