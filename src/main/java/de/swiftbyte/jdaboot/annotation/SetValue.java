package de.swiftbyte.jdaboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a value for a field from the configuration.
 * The value specified in the annotation is the key used to retrieve the value from the configuration.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SetValue {

    /**
     * Specifies the key used to retrieve the value from the configuration.
     *
     * @return The key used to retrieve the value.
     * @since alpha.4
     */
    String value();
}
