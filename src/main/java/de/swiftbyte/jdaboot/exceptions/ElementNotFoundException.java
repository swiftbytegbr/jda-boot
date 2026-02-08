package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;

public class ElementNotFoundException extends RuntimeException {
    public ElementNotFoundException(@NonNull String message, @NonNull Field field) {
        super(String.format("%s (Class: %s, Method: %s)", message, field.getDeclaringClass().getName(), field.getName()));
    }

    public ElementNotFoundException(@NonNull String message, @NonNull String identifier, @NonNull Field field) {
        super(String.format("%s (Identifier: %s, Class: %s, Method: %s)", message, identifier, field.getDeclaringClass().getName(), field.getName()));
    }

    public ElementNotFoundException(@NonNull String message, @NonNull String identifier) {
        super(String.format("%s (Identifier: %s)", message, identifier));
    }
}
