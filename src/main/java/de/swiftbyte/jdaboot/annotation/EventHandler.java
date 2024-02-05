package de.swiftbyte.jdaboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The EventHandler annotation is used to mark a method as an event handler.
 * It includes a property to specify whether the event handler should be executed asynchronously.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

    /**
     * Specifies whether the event handler should be executed asynchronously.
     *
     * @return True if the event handler should be executed asynchronously, false otherwise.
     * @since alpha.4
     */
    boolean async() default false;
}
