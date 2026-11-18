package de.swiftbyte.jdaboot.annotation.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Explicitly includes annotated components, configuration classes and settings classes.
 * Imports may refer to classes outside the application package.
 *
 * @since 1.0.0-beta.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Import {

    /**
     * Specifies the annotated classes to include in metadata discovery.
     *
     * @return The classes imported by this declaration.
     * @since 1.0.0-beta.2
     */
    Class<?>[] value();
}
