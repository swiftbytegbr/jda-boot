package de.swiftbyte.jdaboot.logging;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Central place for Lombok {@code @CustomLog} to obtain loggers with non-null contract.
 *
 * @since 1.0.0-beta.2
 */
public final class LoggerProvider {

    private LoggerProvider() {
        // utility
    }

    /**
     * Obtains a non-null logger for the given class type.
     *
     * @param type the class type for which the logger is to be obtained
     * @return a non-null Logger instance
     */
    public static @NonNull Logger getLogger(@NonNull Class<?> type) {
        return Objects.requireNonNull(LoggerFactory.getLogger(type));
    }
}
