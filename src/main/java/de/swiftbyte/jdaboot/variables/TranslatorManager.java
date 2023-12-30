package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.annotation.SetTranslator;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Modifier;

/**
 * The TranslatorManager class is responsible for managing the translation of text in the application.
 * It uses reflection to find fields annotated with @SetTranslator and sets them to the instance of Translator from JDABoot.
 *
 * @since alpha.4
 */
@Slf4j
public class TranslatorManager {

    /**
     * Constructor for TranslatorManager. Initializes the manager with the specified main class.
     *
     * @param mainClass The main class of your project.
     * @since alpha.4
     */
    public TranslatorManager(Class<?> mainClass) {

        Reflections reflections = new Reflections(mainClass.getPackageName().split("\\.")[0], Scanners.FieldsAnnotated);

        reflections.getFieldsAnnotatedWith(SetTranslator.class).forEach(field -> {

            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    field.set(null, JDABoot.getInstance().getTranslator());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                log.warn("A translator must be a static field. Skipping field " + field + "...");
            }
        });
    }

}
