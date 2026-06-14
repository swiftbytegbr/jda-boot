package de.swiftbyte.jdaboot.scheduler;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.Scheduler;
import de.swiftbyte.jdaboot.exceptions.ElementRegistrationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The SchedulerManager class is responsible for managing scheduled tasks.
 * It discovers scheduled methods and starts them in their configured lifecycle phase.
 *
 * @since alpha.4
 */
@Slf4j
public class SchedulerManager {

    /**
     * Shared executor used by all scheduled tasks.
     */
    private final ScheduledExecutorService executorService;

    /**
     * Tasks waiting for the ready lifecycle phase.
     */
    private final @NonNull List<ScheduledTask> readyTasks = new ArrayList<>();

    /**
     * Ensures that ready tasks are submitted at most once.
     */
    private final AtomicBoolean readySchedulersStarted = new AtomicBoolean();

    /**
     * Constructs a scheduler manager, discovers scheduled methods, and starts initialization tasks.
     *
     * @param mainClass      The main class of the application.
     * @param threadPoolSize The number of threads available for scheduled tasks.
     * @since alpha.4
     */
    public SchedulerManager(@NonNull Class<?> mainClass, int threadPoolSize) {
        Reflections reflections = new Reflections(mainClass.getPackageName(), Scanners.SubTypes.filterResultsBy(c -> true));

        Set<Class<?>> classes = new HashSet<>(reflections.getSubTypesOf(Object.class));

        List<ScheduledTask> initializationTasks = new ArrayList<>();
        for (Class<?> clazz : classes) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Scheduler.class)) {

                    Scheduler scheduler = method.getAnnotation(Scheduler.class);

                    if (scheduler.initialDelay() < 0) {
                        throw new ElementRegistrationException("Scheduler initial delay cannot be negative!", method);
                    }

                    if (scheduler.interval() <= 0) {
                        throw new ElementRegistrationException("Scheduler interval must be greater than 0!", method);
                    }

                    if (method.getParameterCount() != 0) {
                        throw new ElementRegistrationException("Method is annotated with @Scheduler but has parameters!", method);
                    }

                    switch (scheduler.startPhase()) {
                        case INITIALIZATION -> initializationTasks.add(new ScheduledTask(scheduler, method));
                        case READY -> readyTasks.add(new ScheduledTask(scheduler, method));
                    }

                    log.info("Registered scheduler '{}' in class {}", method.getName(), clazz.getName());
                }
            }
        }

        executorService = new ScheduledThreadPoolExecutor(threadPoolSize);

        startSchedulers(initializationTasks);
    }

    /**
     * Starts all tasks configured for the ready phase.
     * Subsequent calls have no effect.
     *
     * @since 1.0.0-beta.2
     */
    public void startReadySchedulers() {
        if (!readySchedulersStarted.compareAndSet(false, true)) {
            return;
        }
        startSchedulers(readyTasks);
    }

    /**
     * Submits the supplied tasks to the scheduler executor.
     *
     * @param tasks The tasks to start.
     * @since 1.0.0-beta.2
     */
    private void startSchedulers(@NonNull List<ScheduledTask> tasks) {
        tasks.forEach(task -> addScheduler(task.scheduler, task.method));
    }

    /**
     * Adds a new scheduled task to the scheduler.
     * The task is represented by a method annotated with the Scheduler annotation.
     *
     * @param scheduler The Scheduler annotation of the task.
     * @param method    The method representing the task.
     * @since alpha.4
     */
    private void addScheduler(@NonNull Scheduler scheduler, @NonNull Method method) {
        ScheduledTaskHandle handle = new ScheduledTaskHandle();
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(() -> {
            try {
                if (method.getReturnType() == boolean.class) {
                    //noinspection DataFlowIssue
                    if (!(boolean) JDABootObjectManager.runMethod(method.getDeclaringClass(), method)) {
                        handle.cancel();
                    }
                } else {
                    JDABootObjectManager.runMethod(method.getDeclaringClass(), method);
                }
            } catch (Exception e) {
                log.error("Exception while executing scheduled task '{}'", method.getName(), e);
            }
        }, scheduler.initialDelay(), scheduler.interval(), TimeUnit.MILLISECONDS);
        handle.setFuture(future);
    }

    /**
     * Associates a scheduler configuration with its method.
     *
     * @param scheduler The scheduler annotation.
     * @param method    The scheduled method.
     * @since 1.0.0-beta.2
     */
    private record ScheduledTask(@NonNull Scheduler scheduler, @NonNull Method method) {
    }

    /**
     * Stores a scheduled future and supports cancellation requests made before the future is assigned.
     */
    private static class ScheduledTaskHandle {
        private final AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
        private final AtomicBoolean cancellationRequested = new AtomicBoolean();

        /**
         * Assigns the scheduled future and applies an earlier cancellation request.
         *
         * @param value The scheduled future.
         * @since 1.0.0-beta.2
         */
        private void setFuture(ScheduledFuture<?> value) {
            future.set(value);

            if (cancellationRequested.get()) {
                value.cancel(false);
            }
        }

        /**
         * Cancels future executions without interrupting an invocation that is already running.
         *
         * @since 1.0.0-beta.2
         */
        private void cancel() {
            cancellationRequested.set(true);

            ScheduledFuture<?> future = this.future.get();
            if (future != null) {
                future.cancel(false);
            }
        }
    }
}
