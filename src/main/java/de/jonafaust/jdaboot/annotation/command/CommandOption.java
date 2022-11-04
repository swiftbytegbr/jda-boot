package de.jonafaust.jdaboot.annotation.command;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface CommandOption {

    OptionType type();

    String name();

    String description();

    int minLength() default 0;

    int maxLength() default 0;

    int minValue() default 0;

    int maxValue() default 0;

    boolean required() default false;

    boolean autoComplete() default false;

    ChannelType[] channelTypes() default {};

    Choice[] optionChoices() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_PARAMETER)
    @interface Choice {

        String name();

        String value();
    }

}
