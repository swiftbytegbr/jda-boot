package de.swiftbyte.jdaboot.configuration;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class Config {

    private static Config instance;
    private static HashMap<String, Object> configDefaultValues = new HashMap<>();
    private static File file;
    private static Properties properties;



    public static final String CONFIG_FILE_NAME = "config.properties";

    protected Config() throws IOException {
        properties = new Properties();
        file = new File(CONFIG_FILE_NAME);
        defaultValues();
        checkFile();
        properties.load(new FileReader(file));
    }

    private Map<String, Object> defaultValues() {
        configDefaultValues.put("discord.token", "BOT TOKEN");
        return configDefaultValues;
    }

    public String getString(String key) {
        String value = properties.getProperty(key);
        if (value == null) configSyntaxError();
        return value;
    }

    public int getInt(String key) {
        String value = properties.getProperty(key);
        if (value == null) configSyntaxError();
        return Integer.parseInt(value);
    }

    public boolean getBoolean(String key) {
        String value = properties.getProperty(key);
        if (value == null) configSyntaxError();
        return Boolean.parseBoolean(value);
    }

    public Config set(String key, Object value) {
        properties.setProperty(key, value.toString());
        return this;
    }

    public void save() {
        try {
            properties.store(new FileWriter(file), "JDABoot Configuration");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Config reload() {
        try {
            properties.load(new FileReader(file));
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addDefaultValue(String key, Object value) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, value.toString());
        }
    }

    public void addDefaultValues(Map<String, Object> values) {
        values.forEach(this::addDefaultValue);
    }

    private void checkFile() throws IOException {
        if(!file.exists()) {
            file.createNewFile();
            addDefaultValues(configDefaultValues);
            save();
            log.info("Created new config file, the system will now exit. Please fill in the values and restart the bot.");
            System.exit(0);
        }
    }

    private void configSyntaxError() {
        log.error("Config syntax error, please check the config file. If you have no idea what to do, delete the config file and restart the bot.\n Then the bot will create a new config file with the default values.");
        System.exit(0);
    }

    public static Config getInstance() {
        if (instance == null) {
            try {
                instance = new Config();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

}
