package de.swiftbyte.jdaboot.configuration;

import de.swiftbyte.jdaboot.JDABootObjectManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A ConfigProvider that chains multiple ConfigProviders together.
 * It will try to retrieve configuration values from each provider in the chain until it finds a value.
 *
 * @see ConfigProvider
 * @since 1.0.0.alpha.5
 */
public class ConfigProviderChain extends ConfigProvider {

    /**
     * The configuration provider chain.
     */
    private List<ConfigProvider> providerChain;

    /**
     * Constructs a new ConfigProviderChain and initializes the provider chain with the default providers.
     *
     * @since 1.0.0.alpha.5
     */
    public ConfigProviderChain() {
        providerChain = new ArrayList<>();

        providerChain.add((ConfigProvider) JDABootObjectManager.getOrInitialiseObject(EnvConfigProviderImpl.class));
        providerChain.add((ConfigProvider) JDABootObjectManager.getOrInitialiseObject(PropertiesConfigProviderImpl.class));
        providerChain.add((ConfigProvider) JDABootObjectManager.getOrInitialiseObject(YmlConfigProviderImpl.class));
        reload();
    }

    /**
     * Adds a ConfigProvider to the chain.
     *
     * @param provider The ConfigProvider to add to the chain.
     * @since 1.0.0.alpha.5
     */
    public void addConfigProviderToChain(ConfigProvider provider) {
        providerChain.add(provider);
        reload();
    }

    /**
     * Gets a ConfigProvider from the chain at the specified index.
     *
     * @param index The index of the ConfigProvider in the chain.
     * @return The ConfigProvider at the specified index.
     * @since 1.0.0.alpha.5
     */
    public ConfigProvider getProviderInChain(int index) {
        return providerChain.get(index);
    }

    /**
     * Gets a ConfigProvider from the chain by its class.
     *
     * @param clazz The class of the ConfigProvider to get.
     * @return The ConfigProvider with the specified class.
     * @since 1.0.0.alpha.5
     */
    public ConfigProvider getProviderInChain(Class<?> clazz) {
        for (ConfigProvider provider : providerChain) {
            if (provider.getClass().equals(clazz)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Sets the configuration profile for all providers in the chain.
     *
     * @param configProfile The configuration profile to use.
     * @since 1.0.0.alpha.5
     */
    @Override
    public void setConfigProfile(String configProfile) {
        for (ConfigProvider provider : providerChain) {
            provider.setConfigProfile(configProfile);
            provider.reload();
        }
    }

    /**
     * Reloads the configuration for all providers in the chain.
     *
     * @since 1.0.0.alpha.5
     */
    @Override
    public void reload() {
        for (ConfigProvider provider : providerChain) {
            provider.reload();
        }
    }

    /**
     * Retrieves the value associated with the specified key in the chain.
     * If the key is not found, it returns the provided default value.
     *
     * @param key          The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since 1.0.0.alpha.5
     */
    @Override
    public Object get(String key, Object defaultValue) {
        for (ConfigProvider provider : providerChain) {
            if (provider.hasKey(key)) {
                return provider.get(key, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Retrieves the string value associated with the specified key in the chain.
     * If the key is not found, it returns the provided default value.
     *
     * @param key          The key of the configuration value.
     * @param defaultValue The default value to return if the key is not found.
     * @return The configuration value.
     * @since 1.0.0.alpha.5
     */
    @Override
    public String getString(String key, String defaultValue) {
        for (ConfigProvider provider : providerChain) {
            if (provider.hasKey(key)) {
                return provider.getString(key, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Retrieves the integer value associated with the specified key in the chain.
     * If the key is not found, it returns 0.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or 0 if the key is not found.
     * @since 1.0.0.alpha.5
     */
    @Override
    public int getInt(String key, int defaultValue) {
        for (ConfigProvider provider : providerChain) {
            if (provider.hasKey(key)) {
                return provider.getInt(key, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Retrieves the boolean value associated with the specified key in the chain.
     * If the key is not found, it returns false.
     *
     * @param key The key of the configuration value.
     * @return The configuration value or false if the key is not found.
     * @since 1.0.0-alpha.5
     */
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        for (ConfigProvider provider : providerChain) {
            if (provider.hasKey(key)) {
                return provider.getBoolean(key, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Checks if the configuration chain contains the specified key.
     *
     * @param key The key to check.
     * @return True if the configuration contains the key, false otherwise.
     * @since 1.0.0.alpha.5
     */
    @Override
    public boolean hasKey(String key) {
        for (ConfigProvider provider : providerChain) {
            if (provider.hasKey(key)) {
                return true;
            }
        }
        return false;
    }
}
