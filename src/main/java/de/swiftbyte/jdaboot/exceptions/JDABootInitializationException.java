package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;

public class JDABootInitializationException extends RuntimeException {
    public JDABootInitializationException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }
}
