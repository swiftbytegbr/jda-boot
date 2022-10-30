package de.jonafaust.jdaboot.annotation.embed;

import net.dv8tion.jda.api.entities.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Embed {

    String url() default "";

    String title() default "";

    String description() default "";

    int color() default Role.DEFAULT_COLOR_RAW;

    String thumbnailUrl() default "";

    EmbedAuthor author() default @EmbedAuthor();

    EmbedFooter footer() default @EmbedFooter();

    String imageUrl() default "";

    EmbedField[] fields() default {};


}
