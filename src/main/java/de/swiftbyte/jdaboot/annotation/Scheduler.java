package de.swiftbyte.jdaboot.annotation;

import de.swiftbyte.jdaboot.scheduler.SchedulerStartPhase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Scheduler annotation is used to mark a method as a scheduled task.
 * It includes properties to specify the interval, initial delay, and lifecycle phase of the task.
 * The annotated method must have no parameters.
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

    /**
     * Specifies the lifecycle phase in which the scheduler should start.
     * By default, the scheduler starts after JDABoot is ready.
     *
     * @return The lifecycle phase in which the scheduler starts.
     * @since 1.0.0-beta.2
     */
    SchedulerStartPhase startPhase() default SchedulerStartPhase.READY;

}
