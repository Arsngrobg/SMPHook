package dev.arsngrobg.smphook.events;

/**
 * <p>The {@code TypeWrapper} interface is a functional interface that transforms a substring into the given generic type {@code T}.</p>
 * 
 * <p>It is intended to be used as positional value types in the {@link EventType} enum, where it matches a positional macro in the <b>prototype string</b>.
 *    For example, the macro:
 *    <blockquote><pre>
 *       [%username%] %message%
 *    </pre></blockquote>
 *    are represented as the positional {@link Event} types:
 *    <blockquote><pre>
 *       ( string, string )
 *    </pre></blockquote>
 *    <i>This is the equivalent type pattern for the {@link EventType#PLAYER_MESSAGE} event type.</i>
 * </p>
 * 
 * @param  T the type to transform the substring to
 * @author Arsngrobg
 * @since  1.0
 */
@FunctionalInterface
public interface TypeWrapper<T> {
    /** <p>The standard {@code TypeWrapper} for {@link java.lang.String}.</p> */
    public static final TypeWrapper<String>  string = String::valueOf;

    /** <p>The standard {@code TypeWrapper} for {@link java.lang.Double}.</p> */
    public static final TypeWrapper<Double>  number = Double::valueOf;

    /** <p>The standard {@code TypeWrapper} for {@link java.lang.Boolean}.</p> */
    public static final TypeWrapper<Boolean> bool   = Boolean::valueOf;

    /**
     * <p>Transforms the given {@code string}, which is a substring of the output from the server, into the type {@code T}.</p>
     * 
     * @return the given {@code string} transformed into type {@code T}
     */
    T asType(String string);
}
