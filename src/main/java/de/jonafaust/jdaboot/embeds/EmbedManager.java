package de.jonafaust.jdaboot.embeds;

import de.jonafaust.jdaboot.annotation.embed.Embed;
import de.jonafaust.jdaboot.annotation.embed.EmbedField;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

@Slf4j
public class EmbedManager {

    private Reflections reflections;

    public EmbedManager(Class<?> mainClass) {

        this.reflections = new Reflections(mainClass.getPackageName().split("\\.")[0], Scanners.FieldsAnnotated);

        this.reflections.getFieldsAnnotatedWith(Embed.class).forEach(field -> {

            Embed embedAnnotation = field.getAnnotation(Embed.class);
            EmbedBuilder embedBuilder = new EmbedBuilder();

            embedBuilder
                    .setTitle(embedAnnotation.title(), embedAnnotation.url())
                    .setDescription(embedAnnotation.description())
                    .setColor(embedAnnotation.color())
                    .setThumbnail(embedAnnotation.thumbnailUrl())
                    .setAuthor(embedAnnotation.author().name(), embedAnnotation.author().url(), embedAnnotation.author().iconUrl())
                    .setFooter(embedAnnotation.footer().text(), embedAnnotation.footer().iconUrl())
                    .setImage(embedAnnotation.imageUrl());

            for (EmbedField embedField : embedAnnotation.fields()) {

                embedBuilder.addField(embedField.title(), embedField.description(), embedField.inline());

            }

            if (field.canAccess(null)) {
                try {
                    field.set(null, embedBuilder.build());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
