package de.swiftbyte.jdaboot.configuration;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import de.swiftbyte.jdaboot.annotation.SetValue;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Modifier;

/**
 * Manages configuration values for the JDABoot framework.
 * It uses reflection to find fields annotated with {@link SetValue} and sets their values based on the configuration.
 *
 * @see SetValue
 * @see JDABootConfigurationManager
 * @since alpha.4
 */
@Slf4j
public class ConfigValueManager {

    /**
     * Constructs a new ConfigValueManager and initializes configuration values.
     *
     * @param mainClass The main class of the application.
     * @since alpha.4
     */
    public ConfigValueManager(Class<?> mainClass) {

        Reflections reflections = new Reflections(mainClass.getPackageName().split("\\.")[0], Scanners.FieldsAnnotated);

        reflections.getFieldsAnnotatedWith(SetValue.class).forEach(field -> {

            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    field.set(null, JDABootConfigurationManager.getConfigProvider().get(field.getAnnotation(SetValue.class).value()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                log.warn("A Set Value must be a static field. Skipping field " + field + "...");
            }
        });
    }

}
