package de.swiftbyte.jdaboot.configuration;

/**
 * Defines the contract for configuration providers.
 * Provides methods to retrieve configuration values of various types, check for the existence of a key, and reload the configuration.
 *
 * @since alpha.4
 */
public abstract class ConfigProvider {

    /**
     * The configuration profile to use.
     * The profile is used to determine which configuration file to use.
     *
     * @since alpha.5
     */
    protected String configProfile = "default";

    /**
     * The next configuration provider in the chain.
     *
     * @since alpha.5
     */
    protected ConfigProvider nextInChain;

    /**
     * Adds a configuration provider to the chain.
     * The provider is added to the end of the chain.
     *
     * @param nextInChain The next configuration provider in the chain.
     * @since alpha.5
     */
    public void addConfigProviderToChain(ConfigProvider nextInChain) {
        if (this.nextInChain == null) this.nextInChain = nextInChain;
        else this.nextInChain.addConfigProviderToChain(nextInChain);
    }

    /**
     * Sets the configuration profile.
     * The profile is used to determine which configuration file to use.
     *
     * @param profile The configuration profile to use.
     * @since alpha.5
     */
    public void setConfigProfile(String profile) {
        this.configProfile = profile;
        if (nextInChain != null) nextInChain.setConfigProfile(profile);
    }

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
    public Object get(String key) {
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
    public abstract Object get(String key, Object defaultValue);

    /**
     * Retrieves the string value associated with the specified key.
     * If the key is not found, it returns null.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or null if the key is not found.
     * @since alpha.4
     */
    public String getString(String key) {
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
    public abstract String getString(String key, String defaultValue);

    /**
     * Retrieves the integer value associated with the specified key.
     * If the key is not found, it returns 0.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or 0 if the key is not found.
     * @since alpha.4
     */
    public int getInt(String key) {
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
    public abstract int getInt(String key, int defaultValue);

    /**
     * Retrieves the boolean value associated with the specified key.
     * If the key is not found, it returns false.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or false if the key is not found.
     * @since alpha.4
     */
    public boolean getBoolean(String key) {
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
    public abstract boolean getBoolean(String key, boolean defaultValue);

    /**
     * Checks if the configuration contains the specified key.
     *
     * @param key The key to check.
     * @return True if the configuration contains the key, false otherwise.
     * @since alpha.4
     */
    public abstract boolean hasKey(String key);
}