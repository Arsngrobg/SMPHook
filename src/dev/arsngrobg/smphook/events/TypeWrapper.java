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
    public static final TypeWrapper<String>  string = String ::valueOf;

    /** <p>The standard {@code TypeWrapper} for {@link java.lang.Long}.</p> */
    public static final TypeWrapper<Long>    i64    = Long   ::valueOf;

    /** <p>The standard {@code TypeWrapper} for {@link java.lang.Integer}.</p> */
    public static final TypeWrapper<Integer> i32    = Integer::valueOf;

    /** <p>The standard {@code TypeWrapper} for {@link java.lang.Short}.</p> */
    public static final TypeWrapper<Short>   i16    = Short  ::valueOf;

    /** <p>The standard {@code TypeWrapper} for {@link java.lang.Byte}.</p> */
    public static final TypeWrapper<Byte>    i8     = Byte   ::valueOf;

    /** <p>The standard {@code TypeWrapper} for {@link java.lang.Double}.</p> */
    public static final TypeWrapper<Double>  f64    = Double ::valueOf;

    /** <p>The standard {@code TypeWrapper} for {@link java.lang.Float}.</p> */
    public static final TypeWrapper<Float>   f32    = Float  ::valueOf;

    /**
     * <p>Transforms the given {@code string}, which is a substring of the output from the server, into the type {@code T}.</p>
     * 
     * @return the given {@code string} transformed into type {@code T}
     */
    T asType(String string);
}
