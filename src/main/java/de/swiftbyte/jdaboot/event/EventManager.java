package de.swiftbyte.jdaboot.event;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.EventHandler;
import de.swiftbyte.jdaboot.exceptions.ElementRegistrationException;
import lombok.CustomLog;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jspecify.annotations.NonNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The EventManager class is responsible for managing events in the application.
 * It uses reflection to find methods annotated with @EventHandler and invokes them when the corresponding event occurs.
 *
 * @since alpha.4
 */
@CustomLog
public class EventManager implements EventListener {

    private @NonNull HashMap<@NonNull Class<@NonNull Event>, @NonNull List<Map.@NonNull Entry<@NonNull Method, @NonNull Object>>> handlers = new HashMap<>();

    private @NonNull ExecutorService executor;

    /**
     * Discovers event handlers below the main package and registers this listener with every shard.
     *
     * @param mainClass    The main class of your project.
     * @param shardManager The shard manager used for event handling.
     * @since alpha.4
     */
    public EventManager(@NonNull Class<?> mainClass, @NonNull ShardManager shardManager) {

        executor = Executors.newCachedThreadPool();

        Reflections reflections = new Reflections(mainClass.getPackageName(), Scanners.SubTypes.filterResultsBy(c -> true));

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
            Class<Event> eventClass = getEventClass(method);

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

        shardManager.addEventListener(this);
    }

    /**
     * Validates an event handler method and returns its event parameter type.
     *
     * @param method The method annotated with {@link EventHandler}.
     * @return The event class accepted by the handler.
     * @throws ElementRegistrationException If the method does not accept exactly one JDA event.
     */
    private static @NonNull Class<@NonNull Event> getEventClass(@NonNull Method method) {
        Class<?>[] params = method.getParameterTypes();

        if (params.length != 1) {
            throw new ElementRegistrationException("Method is annotated with @EventHandler but has more or less than 1 parameter!", method);
        }

        Class<?> firstParam = params[0];

        if (!Event.class.isAssignableFrom(firstParam)) {
            throw new ElementRegistrationException("Method is annotated with @EventHandler but the parameter is not a child of Event!", method);
        }

        //noinspection unchecked
        return (Class<Event>) firstParam;
    }

    /**
     * Handles the specified event. Invokes the corresponding event handlers for the event.
     *
     * @param event The event to handle.
     * @since alpha.4
     */
    @Override
    public void onEvent(@NonNull GenericEvent event) {
        if (handlers.containsKey(event.getClass())) {
            for (Map.Entry<Method, Object> entry : handlers.get(event.getClass())) {
                try {
                    Object instance = entry.getValue();
                    Method method = entry.getKey();
                    boolean async = method.getAnnotation(EventHandler.class).async();
                    if (async) {
                        executor.submit(() -> {
                            try {
                                method.invoke(instance, event);
                            } catch (Exception e) {
                                log.error("Error while invoking event handler {} in class {}", method.getName(), method.getDeclaringClass().getName(), e);
                            }
                        });
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
