package de.swiftbyte.jdaboot;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * The JDABootObjectManager class is responsible for managing objects and their fields and methods.
 * It includes methods to initialize new objects, get objects, inject fields, and run methods.
 *
 * @since 1.0.0-alpha.5
 */
@Slf4j
public class JDABootObjectManager {

    private static HashMap<Class<?>, Object> objectMap = new HashMap<>();

    /**
     * Initializes a new object of the specified class and adds it to the object map.
     *
     * @param clazz The class of the object to initialize.
     * @return The initialized object, or null if the object could not be initialized.
     * @since 1.0.0-alpha.5
     */
    public static Object initialiseNewObject(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            if (constructor.getParameterCount() != 0) {
                log.warn("Failed to initialise new object of class {} because it does not have a no-args constructor! Arg constructors are supported in another version!", clazz.getName());
                return null;
            }

            Object object = clazz.getDeclaredConstructor().newInstance();
            objectMap.put(clazz, object);
            return object;
        } catch (Exception e) {
            log.warn("Failed to initialise new object of class {}!", clazz.getName(), e);
            return null;
        }
    }

    /**
     * Gets the object of the specified class from the object map.
     *
     * @param clazz The class of the object to get.
     * @return The object, or null if the object is not in the map.
     * @since 1.0.0-alpha.5
     */
    public static Object getObject(Class<?> clazz) {
        return objectMap.get(clazz);
    }

    /**
     * Gets the object of the specified class from the object map, or initializes a new object if it is not in the map.
     *
     * @param clazz The class of the object to get or initialize.
     * @return The object, or null if the object could not be initialized.
     * @since 1.0.0-alpha.5
     */
    public static Object getOrInitialiseObject(Class<?> clazz) {
        Object object = getObject(clazz);
        if (object == null) {
            object = initialiseNewObject(clazz);
        }
        return object;
    }

    /**
     * Injects the specified value into the specified field of the specified class.
     *
     * @param clazz The class of the object to inject the field into.
     * @param field The field to inject the value into.
     * @param value The value to inject into the field.
     * @since 1.0.0-alpha.5
     */
    public static void injectField(Class<?> clazz, Field field, Object value) {
        try {

            if (Modifier.isStatic(field.getModifiers())) {
                if (!field.canAccess(null)) {
                    field.setAccessible(true);
                }
                field.set(null, autoCast(field.getType(), value));
            } else {
                Object object = getOrInitialiseObject(clazz);
                if (!field.canAccess(object)) {
                    field.setAccessible(true);
                }
                field.set(object, autoCast(field.getType(), value));
            }
        } catch (Exception e) {
            log.warn("Failed to inject field {} into class {}!", field.getName(), clazz.getName(), e);
        }
    }

    /**
     * Tries to cast a value to a given type. Currently, this only works for primitive types and strings. In future releases
     * we may add an interface to define a custom cast option for custom data types. This method is used to convert a setting
     * value to the needed data type.
     *
     * @param type   The type the given object should be castet to
     * @param object The object to cast
     * @return The castet object
     * @since 1.0.0-alpha.12
     */
    private static Object autoCast(Class<?> type, Object object) {
        if (object instanceof String str) {
            return switch (type.getName()) {
                case "int" -> Integer.valueOf(str);
                case "long" -> Long.valueOf(str);
                case "short" -> Short.valueOf(str);
                case "double" -> Double.valueOf(str);
                case "float" -> Float.valueOf(str);
                case "byte" -> Byte.valueOf(str);
                case "boolean" -> Boolean.parseBoolean(str);
                case "char" -> str.charAt(0);
                default -> object;
            };
        } else if (object instanceof Number num) {
            return switch (type.getName()) {
                case "int" -> num.intValue();
                case "long" -> num.longValue();
                case "short" -> num.shortValue();
                case "double" -> num.doubleValue();
                case "float" -> num.floatValue();
                case "byte" -> num.byteValue();
                default -> object;
            };
        } else if (object instanceof Boolean bool) {
            return bool;
        } else if (object instanceof Character ch) {
            return ch;
        }

        return type.cast(object);
    }

    /**
     * Runs the specified method of the specified class with the specified arguments.
     *
     * @param clazz  The class of the object to run the method on.
     * @param method The method to run.
     * @return The return value of the method, or null if the method could not be run.
     * @since 1.0.0-alpha.5
     */
    public static Object runMethod(Class<?> clazz, Method method) {
        return runMethod(clazz, method, (Object[]) null);
    }

    /**
     * Runs the specified method of the specified class with the specified arguments.
     *
     * @param clazz  The class of the object to run the method on.
     * @param method The method to run.
     * @param args   The arguments to pass to the method.
     * @return The return value of the method, or null if the method could not be run.
     * @since 1.0.0-alpha.5
     */
    public static Object runMethod(Class<?> clazz, Method method, Object... args) {
        try {
            if (Modifier.isStatic(method.getModifiers())) {
                if (!method.canAccess(null)) {
                    method.setAccessible(true);
                }
                return method.invoke(null, args);
            } else {
                Object object = getOrInitialiseObject(clazz);
                if (!method.canAccess(object)) {
                    method.setAccessible(true);
                }
                return method.invoke(object, args);
            }
        } catch (Exception e) {
            log.warn("Failed to run method {} in class {}!", method.getName(), clazz.getName(), e);
            return null;
        }
    }
}
