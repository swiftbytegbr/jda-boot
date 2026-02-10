package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;

public class ElementBuildException extends RuntimeException {
    public ElementBuildException(@NonNull String message, @NonNull Throwable t) {
        super(message, t);
    }

    public ElementBuildException(@NonNull String message, @NonNull String source, @NonNull Throwable t) {
        super(String.format("%s (XML: %s)", message, source), t);
    }
}
