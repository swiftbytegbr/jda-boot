package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;
import java.lang.reflect.Method;

/**
 * Indicates an invalid bean declaration or an application class that cannot be inspected.
 * The message identifies the class or factory method responsible for the failure.
 *
 * @since 1.0.0-beta.2
 */
public class MetadataException extends RuntimeException {

    /**
     * Creates an exception for a class declaration.
     *
     * @param message The description of the metadata error.
     * @param clazz   The class containing the invalid declaration.
     * @since 1.0.0-beta.2
     */
    public MetadataException(@NonNull String message, @NonNull Class<?> clazz) {
        super(String.format("%s (Class: %s)", message, clazz.getName()));
    }

    /**
     * Creates an exception for a factory method declaration.
     *
     * @param message The description of the metadata error.
     * @param method  The factory method containing the invalid declaration.
     * @since 1.0.0-beta.2
     */
    public MetadataException(@NonNull String message, @NonNull Method method) {
        super(String.format("%s (Class: %s, Method: %s)", message, method.getDeclaringClass().getName(), method.getName()));
    }

    /**
     * Creates an exception for a class that could not be loaded for inspection.
     *
     * @param message   The description of the inspection failure.
     * @param className The fully qualified name of the application class.
     * @param cause     The original class-loading failure.
     * @since 1.0.0-beta.2
     */
    public MetadataException(@NonNull String message, @NonNull String className, @NonNull Throwable cause) {
        super(String.format("%s (Class: %s)", message, className), cause);
    }
}
