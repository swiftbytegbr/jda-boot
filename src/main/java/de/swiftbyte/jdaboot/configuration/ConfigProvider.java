package de.swiftbyte.jdaboot.configuration;

/**
 * Defines the contract for configuration providers.
 * Provides methods to retrieve configuration values of various types, check for the existence of a key, and reload the configuration.
 *
 * @since alpha.4
 */
public interface ConfigProvider {

    /**
     * Reloads the configuration.
     *
     * @since alpha.4
     */
    void reload();

    /**
     * Retrieves the value associated with the specified key.
     * If the key is not found, it returns null.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or null if the key is not found.
     * @since alpha.4
     */
    default Object get(String key) {
        return get(key, null);
    }

    /**
     * Retrieves the value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since alpha.4
     */
    Object get(String key, Object defaultValue);

    /**
     * Retrieves the string value associated with the specified key.
     * If the key is not found, it returns null.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or null if the key is not found.
     * @since alpha.4
     */
    default String getString(String key) {
        return getString(key, null);
    }

    /**
     * Retrieves the string value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since alpha.4
     */
    String getString(String key, String defaultValue);

    /**
     * Retrieves the integer value associated with the specified key.
     * If the key is not found, it returns 0.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or 0 if the key is not found.
     * @since alpha.4
     */
    default int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Retrieves the integer value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since alpha.4
     */
    int getInt(String key, int defaultValue);

    /**
     * Retrieves the boolean value associated with the specified key.
     * If the key is not found, it returns false.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or false if the key is not found.
     * @since alpha.4
     */
    default boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Retrieves the boolean value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since alpha.4
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Checks if the configuration contains the specified key.
     *
     * @param key The key to check.
     * @return True if the configuration contains the key, false otherwise.
     * @since alpha.4
     */
    boolean hasKey(String key);
}