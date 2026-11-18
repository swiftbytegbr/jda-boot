package de.swiftbyte.jdaboot.annotation.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a settings class whose values are bound from application configuration.
 *
 * @since 1.0.0-beta.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigurationProperties {

    /**
     * Specifies the configuration key prefix used to bind the settings object.
     *
     * @return The configuration key prefix.
     * @since 1.0.0-beta.2
     */
    String prefix();
}
