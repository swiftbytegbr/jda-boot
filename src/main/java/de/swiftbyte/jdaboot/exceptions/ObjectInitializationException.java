package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;

public class ObjectInitializationException extends RuntimeException {
    public ObjectInitializationException(@NonNull String message, @NonNull Class<?> clazz) {
        super(String.format("%s (Class: %s)", message, clazz.getName()));
    }

    public ObjectInitializationException(@NonNull String message, @NonNull Class<?> clazz, @NonNull String source) {
        super(String.format("%s (Class: %s, XML: %s)", message, clazz.getName(), source));
    }

    public ObjectInitializationException(@NonNull String message, @NonNull String source) {
        super(String.format("%s (XML: %s)", message, source));
    }

    public ObjectInitializationException(@NonNull String message, @NonNull Class<?> clazz, @NonNull Throwable t) {
        super(String.format("%s (Class: %s)", message, clazz.getName()), t);
    }

    public ObjectInitializationException(@NonNull String message, @NonNull Field field, @NonNull Throwable t) {
        super(String.format("%s (Class: %s, Field: %s)", message, field.getDeclaringClass().getName(), field.getName()), t);
    }

    public ObjectInitializationException(@NonNull String message, @NonNull Class<?> clazz, @NonNull String source, @NonNull Throwable t) {
        super(String.format("%s (Class: %s, XML: %s)", message, clazz.getName(), source), t);
    }

    public ObjectInitializationException(@NonNull String message, @NonNull String source, @NonNull Throwable t) {
        super(String.format("%s (XML: %s)", message, source), t);
    }
}
