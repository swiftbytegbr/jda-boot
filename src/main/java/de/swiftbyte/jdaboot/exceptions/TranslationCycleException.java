package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;

public class TranslationCycleException extends RuntimeException {

    public TranslationCycleException(@NonNull String message, @NonNull String loopDetails) {
        super(String.format("%s (Loop: %s)", message, loopDetails));
    }
}
