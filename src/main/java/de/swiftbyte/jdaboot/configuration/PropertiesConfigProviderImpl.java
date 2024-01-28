package de.swiftbyte.jdaboot.configuration;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Implements the ConfigProvider interface using a properties file for configuration.
 * The properties file is named "config.properties" and is expected to be in the classpath.
 *
 * @see ConfigProvider
 * @since alpha.4
 */
@Slf4j
public class PropertiesConfigProviderImpl implements ConfigProvider {

    private Properties properties;

    private static final String CONFIG_FILE_NAME = "config.properties";

    /**
     * Constructs a new PropertiesConfigProviderImpl and reloads the configuration.
     *
     * @since alpha.4
     */
    public PropertiesConfigProviderImpl() {
        reload();
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
    @Override
    public Object get(String key, Object defaultValue) {
        if (!hasKey(key)) {
            return defaultValue;
        }
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
    @Override
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Retrieves the integer value associated with the specified key.
     * If the key is not found, it returns 0.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or 0 if the key is not found.
     * @since alpha.4
     */
    @Override
    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(properties.getProperty(key));
    }

    /**
     * Retrieves the boolean value associated with the specified key.
     * If the key is not found, it returns false.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or false if the key is not found.
     * @since alpha.4
     */
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    /**
     * Checks if the configuration contains the specified key.
     *
     * @param key The key to check.
     * @return True if the configuration contains the key, false otherwise.
     * @since alpha.4
     */
    @Override
    public boolean hasKey(String key) {
        return properties.containsKey(key);
    }

    /**
     * Reloads the configuration from the properties file.
     * If the properties file is not found, it logs an error and exits the application.
     *
     * @since alpha.4
     */
    @Override
    public void reload() {

        properties = new Properties();

        try (InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (resourceStream == null) {
                log.error("Config file not found, please create a config file with the name \"" + CONFIG_FILE_NAME + "\" in your resources.");
                System.exit(1);
            } else {
                properties.load(resourceStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
