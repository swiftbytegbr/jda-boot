package de.swiftbyte.jdaboot;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

@Slf4j
public class JDABootObjectManager {

    private static HashMap<Class<?>, Object> objectMap = new HashMap<>();

    public static Object initialiseNewObject(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();

            if(constructor.getParameterCount() != 0) {
                log.warn("Failed to initialise new object of class " + clazz.getName() + " because it does not have a no-args constructor! Arg constructors are supported in another version!");
                return null;
            }

            Object object = clazz.getDeclaredConstructor().newInstance();
            objectMap.put(clazz, object);
            return object;
        } catch (Exception e) {
            log.warn("Failed to initialise new object of class " + clazz.getName() + "!", e);
            return null;
        }
    }

    public static Object getObject(Class<?> clazz) {
        return objectMap.get(clazz);
    }

    public static Object getOrInitialiseObject(Class<?> clazz) {
        Object object = getObject(clazz);
        if(object == null) {
            object = initialiseNewObject(clazz);
        }
        return object;
    }

    public static void injectField(Class<?> clazz, Field field, Object value) {
        try {

            if(Modifier.isStatic(field.getModifiers())) {
                if(!field.canAccess(null)) field.setAccessible(true);
                field.set(null, value);
            } else {
                Object object = getOrInitialiseObject(clazz);
                if(!field.canAccess(object)) field.setAccessible(true);
                field.set(object, value);
            }
        } catch (Exception e) {
            log.warn("Failed to inject field " + field.getName() + " into class " + clazz.getName() + "!", e);
        }
    }

    public static Object runMethod(Class<?> clazz, Method method) {
        return runMethod(clazz, method, (Object[]) null);
    }

    public static Object runMethod(Class<?> clazz, Method method, Object... args) {
        try {
            if(Modifier.isStatic(method.getModifiers())) {
                if(!method.canAccess(null)) method.setAccessible(true);
                return method.invoke(null, args);
            } else {
                Object object = getOrInitialiseObject(clazz);
                if(!method.canAccess(object)) method.setAccessible(true);
                return method.invoke(object, args);
            }
        } catch (Exception e) {
            log.warn("Failed to run method " + method.getName() + " in class " + clazz.getName() + "!", e);
            return null;
        }
    }
}
