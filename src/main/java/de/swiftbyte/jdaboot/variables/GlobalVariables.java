package de.swiftbyte.jdaboot.variables;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Provides a global storage for variables that can be accessed and modified at runtime.
 * Supports both static and dynamic (Supplier-based) variable values.
 *
 * @since 1.0.0-beta.1
 */
public class GlobalVariables {

    /**
     * Stores the global variables with their corresponding value suppliers.
     *
     * @since 1.0.0-beta.1
     */
    private static HashMap<String, Supplier<String>> variables = new HashMap<>();

    /**
     * Retrieves the value of the specified variable.
     *
     * @param key The name of the variable.
     * @return The value of the variable, or null if the variable does not exist.
     * @since 1.0.0-beta.1
     */
    public static String get(String key) {
        return variables.get(key).get();
    }

    /**
     * Sets the value of the specified variable.
     * The value is stored as a static string.
     *
     * @param key   The name of the variable.
     * @param value The value to set.
     * @since 1.0.0-beta.1
     */
    public static void set(String key, String value) {
        variables.put(key, () -> value);
    }

    /**
     * Sets the value of the specified variable using a Supplier.
     * The value will be dynamically retrieved from the supplier when accessed.
     *
     * @param key   The name of the variable.
     * @param value The supplier providing the value.
     * @since 1.0.0-beta.1
     */
    public static void setDynamicValue(String key, Supplier<String> value) {
        variables.put(key, value);
    }

    /**
     * Checks if the specified variable exists.
     *
     * @param key The name of the variable.
     * @return True if the variable exists, false otherwise.
     * @since 1.0.0-beta.1
     */
    public static boolean hasVariable(String key) {
        return variables.containsKey(key);
    }
}
