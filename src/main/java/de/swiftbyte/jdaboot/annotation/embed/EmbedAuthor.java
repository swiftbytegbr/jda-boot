package de.swiftbyte.jdaboot.annotation.embed;

public @interface EmbedAuthor {

    String name() default "";

    String url() default "";

    String iconUrl() default "";

}
