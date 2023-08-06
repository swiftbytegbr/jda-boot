package de.swiftbyte.jdaboot.annotation.embed;

public @interface EmbedField {

    String title() default "";

    String description() default "";

    boolean inline() default false;

}
