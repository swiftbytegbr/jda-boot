package de.swiftbyte.jdaboot.annotation.context;

import de.swiftbyte.jdaboot.context.BeanPhase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a bean supplied by a factory method on a configuration class.
 * Direct calls to factory methods retain ordinary Java semantics and are not proxied.
 *
 * @since 1.0.0-beta.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bean {

    /**
     * Specifies the unique bean name. An empty value uses the factory method name.
     *
     * @return The explicit bean name, or an empty string.
     * @since 1.0.0-beta.2
     */
    String value() default "";

    /**
     * Specifies when the bean may be created. Bootstrap factories must be static.
     *
     * @return The creation phase of the bean.
     * @since 1.0.0-beta.2
     */
    BeanPhase phase() default BeanPhase.RUNTIME;

    /**
     * Specifies whether the context owns the returned object and its cleanup.
     *
     * @return True if the context manages cleanup; false for externally owned objects.
     * @since 1.0.0-beta.2
     */
    boolean managed() default true;

    /**
     * Specifies an explicit cleanup method. An empty value enables automatic cleanup method selection.
     *
     * @return The cleanup method name, or an empty string.
     * @since 1.0.0-beta.2
     */
    String destroyMethod() default "";
}
