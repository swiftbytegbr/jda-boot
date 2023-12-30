package de.swiftbyte.jdaboot.annotation.command;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The CommandOption annotation is used to specify an option for a command.
 * It includes properties to specify the type, name, description, minimum length, maximum length, minimum value, maximum value,
 * whether the option is required, whether the option supports auto-complete, the channel types for the option, and the choices for the option.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface CommandOption {

    /**
     * The type of the option.
     *
     * @return The type of the option.
     * @since alpha.4
     */
    OptionType type();

    /**
     * The name of the option.
     *
     * @return The name of the option.
     * @since alpha.4
     */
    String name();

    /**
     * The description of the option.
     *
     * @return The description of the option.
     * @since alpha.4
     */
    String description();

    /**
     * The minimum length of the option.
     *
     * @return The minimum length of the option.
     * @since alpha.4
     */
    int minLength() default 0;

    /**
     * The maximum length of the option.
     *
     * @return The maximum length of the option.
     * @since alpha.4
     */
    int maxLength() default 0;

    /**
     * The minimum value of the option.
     *
     * @return The minimum value of the option.
     * @since alpha.4
     */
    int minValue() default 0;

    /**
     * The maximum value of the option.
     *
     * @return The maximum value of the option.
     * @since alpha.4
     */
    int maxValue() default 0;

    /**
     * Specifies whether the option is required.
     *
     * @return True if the option is required, false otherwise.
     * @since alpha.4
     */
    boolean required() default false;

    /**
     * Specifies whether the option supports auto-complete.
     *
     * @return True if the option supports auto-complete, false otherwise.
     * @since alpha.4
     */
    boolean autoComplete() default false;

    /**
     * The channel types for the option.
     *
     * @return The channel types for the option.
     * @since alpha.4
     */
    ChannelType[] channelTypes() default {};

    /**
     * The choices for the option.
     *
     * @return The choices for the option.
     * @since alpha.4
     */
    Choice[] optionChoices() default {};

    /**
     * The Choice annotation is used to specify a choice for a command option.
     * It includes properties to specify the name and value of the choice.
     *
     * @since alpha.4
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_PARAMETER)
    @interface Choice {

        /**
         * The name of the choice.
         *
         * @return The name of the choice.
         * @since alpha.4
         */
        String name();

        /**
         * The value of the choice.
         *
         * @return The value of the choice.
         * @since alpha.4
         */
        String value();
    }

}