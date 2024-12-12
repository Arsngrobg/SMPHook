package arsngrobg.smphook.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1>Signed Integer Parameter</h1>
 * <p>Declares that the integer parameter annotated with this annotation disallows negative values as arguments.</p>
 * 
 * @since  1.0
 * @author Arnsgrobg
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface Signed {}
