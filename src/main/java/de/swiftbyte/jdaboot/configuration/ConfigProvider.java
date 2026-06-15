package de.swiftbyte.jdaboot.configuration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Defines the contract for configuration providers.
 * Provides methods to retrieve configuration values of various types, check for the existence of a key, and reload the configuration.
 *
 * @since alpha.4
 */
public abstract class ConfigProvider {

    /**
     * The active configuration profiles in loading order.
     * The default profile is always the first active profile.
     *
     * @since 1.0.0-beta.2
     */
    protected @NonNull List<@NonNull String> activeProfiles = List.of("default");

    /**
     * Sets the active configuration profiles.
     *
     * @param activeProfiles The additional active profiles in loading order.
     * @since 1.0.0-beta.2
     */
    public void setActiveProfiles(@NonNull List<@NonNull String> activeProfiles) {
        LinkedHashSet<String> normalizedProfiles = new LinkedHashSet<>();
        normalizedProfiles.add("default");
        for (String profile : activeProfiles) {
            String normalizedProfile = profile.trim();
            if (!normalizedProfile.isEmpty() && !normalizedProfile.equals("default")) {
                normalizedProfiles.add(normalizedProfile);
            }
        }
        this.activeProfiles = List.copyOf(normalizedProfiles);
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