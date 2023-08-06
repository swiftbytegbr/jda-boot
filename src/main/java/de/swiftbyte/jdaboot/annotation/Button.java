package de.swiftbyte.jdaboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Button {

    String label();

    Type type();

    String id() default "";

    String emoji() default "";

    String url() default "";


    enum Type {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        LINK
    }
}
