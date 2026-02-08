package de.swiftbyte.jdaboot.configuration;

import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Defines the contract for configuration providers.
 * Provides methods to retrieve configuration values of various types, check for the existence of a key, and reload the configuration.
 *
 * @since alpha.4
 */
@Setter
public abstract class ConfigProvider {

    /**
     * The configuration profile to use.
     * The profile is used to determine which configuration file to use.
     *
     * @since 1.0.0-alpha.5
     */
    protected @NonNull String configProfile = "default";

    /**
     * Reloads the configuration.
     *
     * @since alpha.4
     */
    public abstract void reload();

    /**
     * Retrieves the value associated with the specified key.
     * If the key is not found, it returns null.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or null if the key is not found.
     * @since alpha.4
     */
    public @Nullable Object get(@NonNull String key) {
        return get(key, null);
    }

    /**
     * Retrieves the value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key          The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since alpha.4
     */
    public abstract @NonNull Object get(@NonNull String key, @NonNull Object defaultValue);

    /**
     * Retrieves the string value associated with the specified key.
     * If the key is not found, it returns null.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or null if the key is not found.
     * @since alpha.4
     */
    public @Nullable String getString(@NonNull String key) {
        return getString(key, null);
    }

    /**
     * Retrieves the string value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key          The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since alpha.4
     */
    public abstract @NonNull String getString(@NonNull String key, @NonNull String defaultValue);

    /**
     * Retrieves the integer value associated with the specified key.
     * If the key is not found, it returns 0.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or 0 if the key is not found.
     * @since alpha.4
     */
    public int getInt(@NonNull String key) {
        return getInt(key, 0);
    }

    /**
     * Retrieves the integer value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key          The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since alpha.4
     */
    public abstract int getInt(@NonNull String key, int defaultValue);

    /**
     * Retrieves the boolean value associated with the specified key.
     * If the key is not found, it returns false.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or false if the key is not found.
     * @since alpha.4
     */
    public boolean getBoolean(@NonNull String key) {
        return getBoolean(key, false);
    }

    /**
     * Retrieves the boolean value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key          The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since alpha.4
     */
    public abstract boolean getBoolean(@NonNull String key, boolean defaultValue);

    /**
     * Checks if the configuration contains the specified key.
     *
     * @param key The key to check.
     * @return True if the configuration contains the key, false otherwise.
     * @since alpha.4
     */
    public abstract boolean hasKey(@NonNull String key);
}