package de.swiftbyte.jdaboot.exceptions;

public class VariableCycleException extends RuntimeException {
    public VariableCycleException(String message) {
        super(message);
    }
}
