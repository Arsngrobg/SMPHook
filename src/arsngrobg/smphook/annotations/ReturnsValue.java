package arsngrobg.smphook.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1>Non-Null Return</h1>
 * <p>Declares that the method annotated with this annotation will always return values that are never {@code null}.</p>
 * 
 * @since  1.0
 * @author Arsngrobg
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface ReturnsValue {}
