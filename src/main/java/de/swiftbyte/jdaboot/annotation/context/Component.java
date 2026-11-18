package de.swiftbyte.jdaboot.annotation.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class for automatic constructor-based bean registration.
 *
 * @since 1.0.0-beta.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {

    /**
     * Specifies the unique bean name. An empty value uses the fully qualified class name.
     *
     * @return The explicit bean name, or an empty string.
     * @since 1.0.0-beta.2
     */
    String value() default "";
}
