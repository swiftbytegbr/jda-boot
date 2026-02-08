package de.swiftbyte.jdaboot.configuration;

import de.swiftbyte.jdaboot.exceptions.ConfigurationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implements the ConfigProvider interface using a YAML file for configuration.
 * The YAML file is named "config[-configProfile].yml" and is expected to be in the classpath.
 *
 * @see ConfigProvider
 * @since alpha.4
 */
public class YmlConfigProviderImpl extends ConfigProvider {

    private static @NonNull HashMap<@NonNull String, @NonNull Object> ymlConfig = new HashMap<>();
    private @NonNull String configFileName = "config.yml";

    /**
     * Reloads the configuration from the YAML file.
     * If the YAML file is not found, it logs an error and exits the application.
     *
     * @since alpha.4
     */
    @Override
    public void reload() {

        if (configProfile.equals("default")) {
            configFileName = "config.yml";
        } else {
            configFileName = "config-" + configProfile + ".yml";
        }

        try (InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName)) {
            if (resourceStream != null) {
                ymlConfig = new Yaml().load(resourceStream);
            }
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration", configFileName, e);
        }
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
    public @NonNull Object get(@NonNull String key, @NonNull Object defaultValue) {
        if (hasKey(key)) {
            if (key.contains(".")) {
                Object value = getFromPath(ymlConfig, key);
                return Objects.requireNonNullElse(value, defaultValue);
            } else {
                return ymlConfig.get(key);
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value associated with the specified path from the given map.
     * The path is a dot-separated string representing the hierarchy of keys in the map.
     *
     * @param current The current map to retrieve the value from.
     * @param path    The path of the key.
     * @return The value associated with the specified path, or null if the path is not found.
     * @throws RuntimeException If the path is invalid.
     * @since alpha.4
     */
    private @Nullable Object getFromPath(@NonNull Map<@NonNull String, @NonNull Object> current, @NonNull String path) {
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
            throw new ConfigurationException("Invalid path", configFileName, path);
        }

        //noinspection unchecked
        return getFromPath((Map<String, Object>) next, pathChildren[1]);
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
    public @NonNull String getString(@NonNull String key, @NonNull String defaultValue) {
        return (String) get(key, defaultValue);
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
    @Override
    public int getInt(@NonNull String key, int defaultValue) {
        return (Integer) get(key, defaultValue);
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
    @Override
    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
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
    public boolean hasKey(@NonNull String key) {
        if (key.contains(".")) {
            return getFromPath(ymlConfig, key) != null;
        } else {
            return ymlConfig.containsKey(key);
        }
    }
}
