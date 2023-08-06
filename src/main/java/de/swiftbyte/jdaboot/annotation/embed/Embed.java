package de.swiftbyte.jdaboot.annotation.embed;

import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.embeds.EmbedColor;

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

    EmbedColor color() default EmbedColor.BLACK;

    String hexColor() default "";

    String thumbnailUrl() default "";

    EmbedAuthor author() default @EmbedAuthor();

    EmbedFooter footer() default @EmbedFooter();

    String imageUrl() default "";

    EmbedField[] fields() default {};

    DefaultVariable[] defaultVars() default {};


}
