package de.jonafaust.jdaboot.annotation.embed;

public @interface EmbedField {

    String title() default "";

    String description() default "";

    boolean inline() default false;

}
