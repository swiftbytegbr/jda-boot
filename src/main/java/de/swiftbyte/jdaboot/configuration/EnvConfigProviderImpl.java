package de.swiftbyte.jdaboot.configuration;

/**
 * Implements the ConfigProvider interface using env variables for configuration.
 *
 * @see ConfigProvider
 * @since 1.0.0-alpha.5
 */
public class EnvConfigProviderImpl extends ConfigProvider {

    /**
     * Constructs a new EnvConfigProviderImpl and reloads the configuration.
     *
     * @since 1.0.0-alpha.5
     */
    @Override
    public void reload() {
    }

    /**
     * Retrieves the value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key          The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since 1.0.0-alpha.5
     */
    @Override
    public Object get(String key, Object defaultValue) {
        return getString(key, (String) defaultValue);
    }

    /**
     * Retrieves the string value associated with the specified key.
     * If the key is not found, it returns the provided default value.
     *
     * @param key          The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since 1.0.0-alpha.5
     */
    @Override
    public String getString(String key, String defaultValue) {
        if (System.getenv(key) != null) return System.getenv(key);
        else if (System.getProperty(key) != null) return System.getProperty(key);
        else return defaultValue;
    }

    /**
     * Retrieves the integer value associated with the specified key.
     * If the key is not found, it returns 0.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or 0 if the key is not found.
     * @since 1.0.0-alpha.5
     */
    @Override
    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(getString(key, String.valueOf(defaultValue)));
    }

    /**
     * Retrieves the boolean value associated with the specified key.
     * If the key is not found, it returns false.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or false if the key is not found.
     * @since 1.0.0-alpha.5
     */
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getString(key, String.valueOf(defaultValue)));
    }

    /**
     * Checks if the configuration contains the specified key.
     *
     * @param key The key to check.
     * @return True if the configuration contains the key, false otherwise.
     * @since 1.0.0-alpha.5
     */
    @Override
    public boolean hasKey(String key) {
        if (System.getenv(key) != null) return true;
        else return System.getProperty(key) != null;
    }
}
