package de.swiftbyte.jdaboot.configuration;

import java.util.Map;

public interface Config {
    String getString(String key);

    int getInt(String key);

    boolean getBoolean(String key);

    Config set(String key, Object value);

    void save();

    Config reload();

    void addDefaultValue(String key, Object value);

    void addDefaultValues(Map<String, Object> values);
}