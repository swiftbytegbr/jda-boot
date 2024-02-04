package de.swiftbyte.jdaboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Scheduler annotation is used to mark a method as a scheduled task.
 * It includes properties to specify the interval and initial delay of the task.
 * The method marked with this annotation should have no parameters and be static.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Scheduler {

    /**
     * The interval at which the task should be run, in milliseconds.
     *
     * @return The interval of the task.
     * @since alpha.4
     */
    int interval();

    /**
     * The initial delay before the task is run for the first time, in milliseconds.
     * By default, there is no delay.
     *
     * @return The initial delay of the task.
     * @since alpha.4
     */
    int initialDelay() default 0;

}
