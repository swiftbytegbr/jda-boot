package de.swiftbyte.jdaboot.scheduler;

/**
 * Defines when a scheduled task is submitted to the scheduler executor.
 *
 * @since 1.0.0-beta.2
 */
public enum SchedulerStartPhase {
    /**
     * Starts the task while JDABoot is being initialized.
     */
    INITIALIZATION,

    /**
     * Starts the task after all managed JDA shards are ready.
     */
    READY
}
