package de.jonafaust.jdaboot.variables;

import de.jonafaust.jdaboot.JDABoot;
import de.jonafaust.jdaboot.annotation.SetTranslator;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Modifier;

@Slf4j
public class TranslatorManager {

    private Reflections reflections;

    public TranslatorManager(Class<?> mainClass) {

        this.reflections = new Reflections(mainClass.getPackageName().split("\\.")[0], Scanners.FieldsAnnotated);

        this.reflections.getFieldsAnnotatedWith(SetTranslator.class).forEach(field -> {

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
