package de.jonafaust.jdaboot.embeds;

import de.jonafaust.jdaboot.annotation.embed.Embed;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Modifier;

@Slf4j
public class EmbedManager {

    private Reflections reflections;

    public EmbedManager(Class<?> mainClass) {

        this.reflections = new Reflections(mainClass.getPackageName().split("\\.")[0], Scanners.FieldsAnnotated);

        this.reflections.getFieldsAnnotatedWith(Embed.class).forEach(field -> {

            Embed embedAnnotation = field.getAnnotation(Embed.class);

            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    field.set(null, new TemplateEmbed(embedAnnotation));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                log.warn("An embed must either be a static field or created in a class managed by JDA-Boot. Skipping field " + field + "...");
            }
        });
    }
}
