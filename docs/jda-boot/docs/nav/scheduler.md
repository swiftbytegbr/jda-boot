# Scheduler

The Scheduler system in JDA-Boot allows you to execute recurring tasks at specified intervals. Schedulers are configured using the `@Scheduler` annotation and can be used to automate periodic actions in your bot. This page explains how to configure and use schedulers in your project.

## Scheduler Configuration

To create a scheduler, a method must be annotated with the `@Scheduler` annotation. The method will be executed periodically based on the specified interval and initial delay.

### @Scheduler

The `@Scheduler` annotation is used to define the configuration of a scheduler.

| Annotation Field | Description                                                                   | Data Type |
|------------------|-------------------------------------------------------------------------------|-----------|
| `interval`       | The interval in milliseconds between each execution of the scheduler          | long      |
| `initialDelay`   | The delay in milliseconds before the scheduler is executed for the first time | long      |

### Method Requirements

- The method annotated with `@Scheduler` must return a `void` or `boolean`.
- If the return type is `boolean`, returning `true` keeps the scheduler running.
- If the return type is `boolean`, returning `false` stops the scheduler.

## Scheduler Execution

When the bot starts, all methods annotated with `@Scheduler` are automatically registered and executed based on their configuration. The scheduler will continue to run until the method returns `false`.

!!! example
    === "Java"
        ```java
        public class ExampleScheduler {

            private int counter = 0;

            @Scheduler(interval = 10000, initialDelay = 5000)
            private boolean runScheduler() {
                counter++;
                System.out.println("Scheduler executed " + counter + " times.");
                return counter < 5; // Stops after 5 executions
            }
        }
        ```

In this example, the `runScheduler` method is executed every 10 seconds, starting 5 seconds after the bot starts. The scheduler stops after 5 executions.