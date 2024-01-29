package de.swiftbyte.jdaboot;

import de.swiftbyte.jdaboot.annotation.AutoConfiguration;
import de.swiftbyte.jdaboot.interactions.buttons.ButtonManager;
import de.swiftbyte.jdaboot.interactions.commands.CommandManager;
import de.swiftbyte.jdaboot.configuration.ConfigProvider;
import de.swiftbyte.jdaboot.configuration.ConfigValueManager;
import de.swiftbyte.jdaboot.embeds.EmbedManager;
import de.swiftbyte.jdaboot.event.EventManager;
import de.swiftbyte.jdaboot.variables.TranslationProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Manages the configuration for the JDABoot framework.
 * It applies the configuration specified by the {@link AutoConfiguration} annotation and initializes various managers.
 *
 * @see AutoConfiguration
 * @see ConfigProvider
 * @see TranslationProvider
 * @see CommandManager
 * @see ButtonManager
 * @see ConfigValueManager
 * @see EventManager
 * @see EmbedManager
 * @since alpha.4
 */
@Slf4j
public class JDABootConfigurationManager {

    @Getter(AccessLevel.PROTECTED)
    private static List<GatewayIntent> intents;

    @Getter(AccessLevel.PROTECTED)
    private static List<CacheFlag> enabledCacheFlags;

    @Getter(AccessLevel.PROTECTED)
    private static List<CacheFlag> disabledCacheFlags;

    @Getter(AccessLevel.PROTECTED)
    private static MemberCachePolicy memberCachePolicy;


    /**
     * The configuration provider used to retrieve configuration values.
     *
     * @since alpha.4
     */
    @Getter
    @Setter
    private static ConfigProvider configProvider;

    /**
     * The translation provider used to retrieve translations.
     *
     * @since alpha.4
     */
    @Getter
    @Setter
    private static TranslationProvider translationProvider;


    @Getter(AccessLevel.PROTECTED)
    private static CommandManager commandManager;

    @Getter(AccessLevel.PUBLIC)
    private static ButtonManager buttonManager;

    /**
     * Applies the configuration specified by the {@link AutoConfiguration} annotation.
     *
     * @param mainClass The main class of the application.
     * @since alpha.4
     */
    protected static void autoConfigure(Class<?> mainClass) {
        AutoConfiguration autoConfiguration = mainClass.getAnnotation(AutoConfiguration.class);
        if (autoConfiguration == null) {
            autoConfiguration = JDABoot.class.getAnnotation(AutoConfiguration.class);
        }
        applyAutoConfiguration(autoConfiguration);
    }

    /**
     * Applies the configuration specified by the {@link AutoConfiguration} annotation.
     *
     * @param autoConfiguration The AutoConfiguration annotation to apply.
     * @since alpha.4
     */
    private static void applyAutoConfiguration(@NotNull AutoConfiguration autoConfiguration) {
        try {
            configProvider = autoConfiguration.configProvider().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.error("Failed to instantiate config provider", e);
            System.exit(1);
        }

        try {
            translationProvider = autoConfiguration.translationProvider().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            log.error("Failed to instantiate translation provider", e);
            System.exit(1);
        }

        intents = List.of(autoConfiguration.intents());
        enabledCacheFlags = List.of(autoConfiguration.enabledCacheFlags());
        disabledCacheFlags = List.of(autoConfiguration.disabledCacheFlags());
        memberCachePolicy = autoConfiguration.memberCachePolicy().getJDAUtilsMemberCachePolicy();
    }

    /**
     * Initializes various managers.
     *
     * @param mainClass The main class of the application.
     * @param jda       The JDA instance.
     * @since alpha.4
     */
    protected static void initialiseManagers(Class<?> mainClass, JDA jda) {
        commandManager = new CommandManager(jda, mainClass);
        buttonManager = new ButtonManager(jda, mainClass);

        new ConfigValueManager(mainClass);
        new EventManager(jda, mainClass);
        new EmbedManager(mainClass);
    }
}
