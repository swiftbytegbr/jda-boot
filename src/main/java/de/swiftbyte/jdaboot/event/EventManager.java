package de.swiftbyte.jdaboot.event;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.EventHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The EventManager class is responsible for managing events in the application.
 * It uses reflection to find methods annotated with @EventHandler and invokes them when the corresponding event occurs.
 *
 * @since alpha.4
 */
@Slf4j
public class EventManager implements EventListener {

    Reflections reflections;

    HashMap<Class<Event>, List<Map.Entry<Method, Object>>> handlers = new HashMap<>();

    JDA jda;

    /**
     * Constructor for EventManager. Initializes the manager with the specified JDA instance and main class.
     *
     * @param jda       The JDA instance to use for event handling.
     * @param mainClass The main class of your project.
     * @since alpha.4
     */
    public EventManager(JDA jda, Class<?> mainClass) {
        this.jda = jda;
        reflections = new Reflections(mainClass.getPackageName(), Scanners.SubTypes.filterResultsBy(c -> true));

        Set<Class<?>> classes = new HashSet<>(reflections.getSubTypesOf(Object.class));
        Set<Method> methods = new HashSet<>();


        for (Class<?> clazz : classes) {
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(EventHandler.class)) {
                    methods.add(method);
                }
            }
        }

        for (Method method : methods) {
            Class<?>[] params = method.getParameterTypes();

            try {
                method.getDeclaringClass().getConstructor();
            } catch (NoSuchMethodException e) {
                log.error("The class {} has no no args constructor, this is required for event handling. Ignoring the method {}", method.getDeclaringClass().getName(), method.getName());
                continue;
            }

            try {
                method.getDeclaringClass().getConstructor();
            } catch (NoSuchMethodException e) {
                log.warn("Found @EventHandler method {}.{} in a class without a no args constructor! Skipping...", method.getClass().getName(), method.getName());
            }

            if (params.length != 1) {
                log.warn("Found @EventHandler method {}.{} with more or less than 1 parameter! Skipping...", method.getClass().getName(), method.getName());
                continue;
            }

            Class<?> firstParam = params[0];

            if (!Event.class.isAssignableFrom(firstParam)) {
                log.warn("Found @EventHandler method {}.{} with a parameter that is not a child of Event! Skipping...", method.getClass().getName(), method.getName());
                continue;
            }

            Class<Event> eventClass = (Class<Event>) firstParam;

            Object instance = JDABootObjectManager.getOrInitialiseObject(method.getDeclaringClass());

            if (handlers.containsKey(eventClass)) {
                List<Map.Entry<Method, Object>> list = handlers.get(eventClass);
                list = new ArrayList<>(list);
                list.add(Map.entry(method, instance));
                handlers.put(eventClass, list);
            } else {
                handlers.put(eventClass, List.of(Map.entry(method, instance)));
            }
            log.info("Registered event handler for {}", eventClass.getName());
        }

        jda.addEventListener(this);
    }

    /**
     * Handles the specified event. Invokes the corresponding event handlers for the event.
     *
     * @param event The event to handle.
     * @since alpha.4
     */
    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (handlers.containsKey(event.getClass())) {
            for (Map.Entry<Method, Object> entry : handlers.get(event.getClass())) {
                try {
                    Object instance = entry.getValue();
                    Method method = entry.getKey();
                    boolean async = method.getAnnotation(EventHandler.class).async();
                    if (async) {
                        new Thread(() -> {
                            try {
                                method.invoke(instance, event);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                log.error("Error while invoking event handler {} in class {}", method.getName(), method.getDeclaringClass().getName(), e);
                            }
                        }).start();
                    } else {
                        method.invoke(instance, event);
                    }
                } catch (Exception e) {
                    log.error("Error while invoking an event handler", e);
                }
            }
        }
    }
}
