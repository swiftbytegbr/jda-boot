package de.swiftbyte.jdaboot.variables;

import java.util.HashMap;
import java.util.function.Supplier;

public class GlobalVariables {

    private static HashMap<String, Supplier<String>> variables = new HashMap<>();

    public static String get(String key) {
        return variables.get(key).get();
    }

    public static void set(String key, String value) {
        variables.put(key, () -> value);
    }

    public static void setDynamicValue(String key, Supplier<String> value) {
        variables.put(key, value);
    }

    public static boolean hasVariable(String key) {
        return variables.containsKey(key);
    }
}
