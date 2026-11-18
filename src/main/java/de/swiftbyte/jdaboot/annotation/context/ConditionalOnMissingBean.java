package de.swiftbyte.jdaboot.annotation.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the types whose absence enables a factory in default configuration.
 * Discovery records the condition; dependency resolution evaluates it later.
 *
 * @since 1.0.0-beta.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConditionalOnMissingBean {

    /**
     * Specifies the offered types that must be absent for the factory to be enabled.
     *
     * @return The types checked before creating the default bean.
     * @since 1.0.0-beta.2
     */
    Class<?>[] value();
}
