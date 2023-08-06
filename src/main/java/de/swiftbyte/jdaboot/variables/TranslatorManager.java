package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.annotation.SetTranslator;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Modifier;

@Slf4j
public class TranslatorManager {

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
