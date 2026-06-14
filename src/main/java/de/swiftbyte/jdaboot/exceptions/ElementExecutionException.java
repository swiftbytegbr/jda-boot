package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ElementExecutionException extends RuntimeException {
    public ElementExecutionException(@NonNull String message, @NonNull Method method, @NonNull Throwable cause) {
        super(String.format("%s (Class: %s, Method: %s)", message, method.getDeclaringClass().getName(), method.getName()), cause);
    }
}
