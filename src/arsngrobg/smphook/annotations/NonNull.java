package arsngrobg.smphook.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1>Non-Null Parameter</h1>
 * <p>Declares that the parameter annotated with this annotation disallows {@code null} pointers as arguments.</p>
 * 
 * @since  1.0
 * @author Arnsgrobg
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface NonNull {}
