package de.swiftbyte.jdaboot.exceptions;

public class TranslationCycleException extends RuntimeException {
    public TranslationCycleException(String message) {
        super(message);
    }
}
