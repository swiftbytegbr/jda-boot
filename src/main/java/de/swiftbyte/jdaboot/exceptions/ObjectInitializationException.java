package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;

public class ObjectInitializationException extends RuntimeException {
    public ObjectInitializationException(@NonNull String message, @NonNull Class<?> clazz) {
        super(String.format("%s (Class: %s)", message, clazz.getName()));
    }

    public ObjectInitializationException(@NonNull String message, @NonNull Class<?> clazz, @NonNull Throwable t) {
        super(String.format("%s (Class: %s)", message, clazz.getName()), t);
    }
}
