package de.swiftbyte.jdaboot.exceptions;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message, String file) {
        super(String.format("%s (File: %s)", message, file));
    }

    public ConfigurationException(String message, String file, Throwable t) {
        super(String.format("%s (File: %s)", message, file), t);
    }

    public ConfigurationException(String message, String file, String path) {
        super(String.format("%s (File: %s, Path: %s)", message, file, path));
    }
}
