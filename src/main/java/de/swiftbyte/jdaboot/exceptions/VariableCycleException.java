package de.swiftbyte.jdaboot.exceptions;

import org.jspecify.annotations.NonNull;

public class VariableCycleException extends RuntimeException {
    public VariableCycleException(@NonNull String message, @NonNull String loopDetails) {
        super(String.format("%s (Loop: %s)", message, loopDetails));
    }
}
