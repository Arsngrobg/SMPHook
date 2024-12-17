package arsngrobg.smphook.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1>Utility Class</h1>
 * <p>Declares that the class annotated with this annotation is referred to as a utility class.
 *    This means that it does not require an instance of this class in order to use it, and any instances are non-beneficial.
 * </p>
 * 
 * @since  1.0
 * @author Arsngrobg
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface UtilityClass {}
