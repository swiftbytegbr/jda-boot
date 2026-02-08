package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;

public class ElementRegistrationException extends RuntimeException {
    public ElementRegistrationException(@NonNull String message, @NonNull Class<?> clazz) {
        super(String.format("%s (Class: %s)", message, clazz.getName()));
    }

    public ElementRegistrationException(@NonNull String message, @NonNull Method method) {
        super(String.format("%s (Class: %s, Method: %s)", message, method.getDeclaringClass().getName(), method.getName()));
    }
}
