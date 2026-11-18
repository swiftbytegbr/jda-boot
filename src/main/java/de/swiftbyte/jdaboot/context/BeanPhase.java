package de.swiftbyte.jdaboot.context;

/**
 * Distinguishes early configuration infrastructure from ordinary application beans.
 *
 * @since 1.0.0-beta.2
 */
public enum BeanPhase {

    /**
     * Creation before settings binding for early configuration infrastructure.
     *
     * @since 1.0.0-beta.2
     */
    BOOTSTRAP,

    /**
     * Creation after the configuration environment and settings have been prepared.
     *
     * @since 1.0.0-beta.2
     */
    RUNTIME
}
