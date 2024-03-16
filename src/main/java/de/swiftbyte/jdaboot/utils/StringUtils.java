package de.swiftbyte.jdaboot.utils;

/**
 * The StringUtils class provides utility methods for working with strings.
 *
 * @since 1.0.0.alpha.5

 */
public class StringUtils {

    /**
     * Checks if the specified string is blank.
     *
     * @param string The string to check.
     * @return True if the string is blank, false otherwise.
     * @since 1.0.0.alpha.5
     */
    public static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     * Checks if the specified string is not blank.
     *
     * @param string The string to check.
     * @return True if the string is not blank, false otherwise.
     * @since 1.0.0.alpha.5
     */
    public static boolean isNotBlank(String string) {
        return !isBlank(string);
    }
}
