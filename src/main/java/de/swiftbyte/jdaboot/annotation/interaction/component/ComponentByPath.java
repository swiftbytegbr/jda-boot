package de.swiftbyte.jdaboot.annotation.interaction.component;

import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject a Component V2 template by XML resource path.
 * The path is resolved relative to {@code resources/components} when no prefix is given.
 *
 * @since 1.0.0-beta.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ComponentByPath {

    /**
     * XML path in the classpath. If no {@code components/} prefix is present, it is added automatically.
     *
     * @return Component XML path.
     * @since 1.0.0-beta.2
     */
    @NonNull String value();

    /**
     * Optional layout id inside the XML file. When omitted, the XML file must define exactly one layout.
     *
     * @return Layout id.
     * @since 1.0.0-beta.2
     */
    @NonNull String layoutId() default "";

    /**
     * The default variables available to the injected Component V2 template.
     *
     * @return The default variables.
     * @since 1.0.0-beta.2
     */
    @NonNull DefaultVariable @NonNull [] defaultVars() default {};
}
