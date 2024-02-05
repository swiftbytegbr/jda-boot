package de.swiftbyte.jdaboot.scheduler;

import de.swiftbyte.jdaboot.annotation.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The SchedulerManager class is responsible for managing scheduled tasks.
 * It includes methods to register tasks and to start the scheduler thread.
 *
 * @since alpha.4
 */
@Slf4j
public class SchedulerManager {

    Reflections reflections;

    /**
     * Constructs a new SchedulerManager and registers all methods annotated with Scheduler.
     *
     * @param mainClass The main class of the application.
     * @since alpha.4
     */
    public SchedulerManager(Class<?> mainClass) {
        reflections = new Reflections(mainClass.getPackageName(), Scanners.SubTypes.filterResultsBy(c -> true));

        Set<Class<?>> classes = new HashSet<>(reflections.getSubTypesOf(Object.class));

        for (Class<?> clazz : classes) {
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(Scheduler.class)) {

                    Scheduler scheduler = method.getAnnotation(Scheduler.class);

                    if (method.getParameterCount() != 0) {
                        log.error("Method " + method.getName() + " in class " + clazz.getSimpleName() + " is annotated with @Scheduler but has parameters!");
                        continue;
                    }

                    if (!Modifier.isStatic(method.getModifiers())) {
                        log.error("Method " + method.getName() + " in class " + clazz.getSimpleName() + " is annotated with @Scheduler but is not static!");
                        continue;
                    }

                    addScheduler(scheduler, method);
                }
            }
        }
    }

    /**
     * Adds a new scheduled task to the scheduler.
     * The task is represented by a method annotated with the Scheduler annotation.
     *
     * @param scheduler The Scheduler annotation of the task.
     * @param method    The method representing the task.
     * @since alpha.4
     */
    private void addScheduler(Scheduler scheduler, Method method) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (method.getReturnType() == boolean.class) {
                        if (!(boolean) method.invoke(null)) {
                            timer.cancel();
                        }
                    } else {
                        method.invoke(null);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Error while invoking scheduler " + method.getName() + "!", e);
                }
            }
        }, scheduler.initialDelay(), scheduler.interval());
    }
}
