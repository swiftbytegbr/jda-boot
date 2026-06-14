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
 * It includes methods to register tasks and to start the scheduler thread.
 *
 * @since alpha.4
 */
@Slf4j
public class SchedulerManager {

    private final ScheduledExecutorService executorService;

    private final @NonNull List<ScheduledTask> readyTasks = new ArrayList<>();

    private final AtomicBoolean readySchedulersStarted = new AtomicBoolean();

    /**
     * Constructs a new SchedulerManager and registers all methods annotated with Scheduler.
     *
     * @param mainClass The main class of the application.
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

                    if(scheduler.initialDelay() < 0) {
                        throw new ElementRegistrationException("Scheduler initial delay cannot be negative!", method);
                    }

                    if(scheduler.interval() <= 0) {
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

    public void startReadySchedulers() {
        if(!readySchedulersStarted.compareAndSet(false, true)) return;
        startSchedulers(readyTasks);
    }

    private void startSchedulers(List<ScheduledTask> tasks) {
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

    private record ScheduledTask(@NonNull Scheduler scheduler, @NonNull Method method) {}

    private static class ScheduledTaskHandle {
        private final AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
        private final AtomicBoolean cancellationRequested = new AtomicBoolean();

        private void setFuture(ScheduledFuture<?> value) {
            future.set(value);

            if(cancellationRequested.get()) value.cancel(false);
        }

        private void cancel() {
            cancellationRequested.set(true);

            ScheduledFuture<?> future = this.future.get();
            if (future != null) {
                future.cancel(false);
            }
        }
    }
}
