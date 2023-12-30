package de.swiftbyte.jdaboot.configuration;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the ConfigProvider interface using a YAML file for configuration.
 * The YAML file is named "config.yml" and is expected to be in the classpath.
 *
 * @since alpha.4
 * @see ConfigProvider
 */
@Slf4j
public class YmlConfigProviderImpl implements ConfigProvider {

    private static HashMap<String, Object> ymlConfig = new HashMap<>();

    private static final String CONFIG_FILE_NAME = "config.yml";

    /**
     * Constructs a new YmlConfigProviderImpl and reloads the configuration.
     *
     * @since alpha.4
     */
    public YmlConfigProviderImpl() {
        reload();
    }

    /**
     * Reloads the configuration from the YAML file.
     * If the YAML file is not found, it logs an error and exits the application.
     *
     * @since alpha.4
     */
    @Override
    public void reload() {
        try (InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if(resourceStream == null) {
                log.error("Config file not found, please create a config file with the name \"" + CONFIG_FILE_NAME + "\" in your resources.");
                System.exit(1);
            } else {
                ymlConfig = new Yaml().load(resourceStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    @Override
    public Object get(String key, Object defaultValue) {
        if(hasKey(key)) {

            if(key.contains(".")) {
                return getFromPath(ymlConfig, key);
            } else return ymlConfig.get(key);

        } else {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value associated with the specified path from the given map.
     * The path is a dot-separated string representing the hierarchy of keys in the map.
     *
     * @param current The current map to retrieve the value from.
     * @param path The path of the key.
     * @return The value associated with the specified path, or null if the path is not found.
     * @throws RuntimeException If the path is invalid.
     * @since alpha.4
     */
    private Object getFromPath(Map<String, Object> current, String path) {
        String[] pathChildren = path.split("\\.", 2);
        String firstPart = pathChildren[0];

        if (!current.containsKey(firstPart)) {
            return null;
        }

        if (pathChildren.length == 1) {
            return current.get(firstPart);
        }

        Object next = current.get(firstPart);
        if (!(next instanceof Map)) {
            throw new RuntimeException("Invalid path: " + path);
        }

        //noinspection unchecked
        return getFromPath((Map<String, Object>) next, pathChildren[1]);
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
    @Override
    public String getString(String key, String defaultValue) {
        return (String) get(key, defaultValue);
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
    @Override
    public int getInt(String key, int defaultValue) {
        return (Integer) get(key, defaultValue);
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
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return (Boolean) get(key, defaultValue);
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
        if(key.contains(".")) {
            return getFromPath(ymlConfig, key) != null;
        } else return ymlConfig.containsKey(key);
    }
}
