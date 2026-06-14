package de.swiftbyte.jdaboot;

import de.swiftbyte.jdaboot.annotation.JDABootConfiguration;
import de.swiftbyte.jdaboot.cli.ConsoleCommandManager;
import de.swiftbyte.jdaboot.configuration.ConfigProvider;
import de.swiftbyte.jdaboot.configuration.ConfigProviderChain;
import de.swiftbyte.jdaboot.configuration.ConfigValueManager;
import de.swiftbyte.jdaboot.embed.EmbedManager;
import de.swiftbyte.jdaboot.event.EventManager;
import de.swiftbyte.jdaboot.exceptions.ObjectInitializationException;
import de.swiftbyte.jdaboot.exceptions.StillInitializingException;
import de.swiftbyte.jdaboot.interaction.button.ButtonManager;
import de.swiftbyte.jdaboot.interaction.command.CommandManager;
import de.swiftbyte.jdaboot.interaction.component.v2.ComponentV2Manager;
import de.swiftbyte.jdaboot.interaction.modal.ModalManager;
import de.swiftbyte.jdaboot.interaction.selection.SelectMenuManager;
import de.swiftbyte.jdaboot.scheduler.SchedulerManager;
import de.swiftbyte.jdaboot.variables.GlobalVariables;
import de.swiftbyte.jdaboot.variables.TranslationProvider;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Manages the configuration for the JDABoot framework.
 * It applies the configuration specified by the {@link JDABootConfiguration} annotation and initializes various managers.
 *
 * @see JDABootConfiguration
 * @see ConfigProvider
 * @see TranslationProvider
 * @see CommandManager
 * @see ConsoleCommandManager
 * @see ButtonManager
 * @see ConfigValueManager
 * @see EventManager
 * @see EmbedManager
 * @see SchedulerManager
 * @since alpha.4
 */
@CustomLog
public final class JDABootConfigurationManager {

    private static @Nullable List<@NonNull GatewayIntent> intents;

    private static @Nullable List<@NonNull CacheFlag> enabledCacheFlags;

    private static @Nullable List<@NonNull CacheFlag> disabledCacheFlags;

    @Getter(AccessLevel.PROTECTED)
    private static @Nullable MemberCachePolicy memberCachePolicy;


    /**
     * The configuration provider chain used to retrieve configuration values.
     *
     * @since 1.0.0-alpha.5
     */
    private static @Nullable ConfigProviderChain configProviderChain;

    /**
     * The translation provider used to retrieve translations.
     *
     * @since alpha.4
     */
    private static @Nullable TranslationProvider translationProvider;


    private static @Nullable CommandManager commandManager;

    @Getter(AccessLevel.PUBLIC)
    private static @Nullable ButtonManager buttonManager;

    @Getter(AccessLevel.PUBLIC)
    private static @Nullable ModalManager modalManager;

    @Getter(AccessLevel.PUBLIC)
    private static @Nullable SelectMenuManager selectMenuManager;

    @Getter(AccessLevel.PUBLIC)
    private static @Nullable ComponentV2Manager componentV2Manager;

    private static boolean consoleCommandsEnabled;

    private JDABootConfigurationManager() {
        //utility
    }

    /**
     * Applies the configuration specified by the {@link JDABootConfiguration} annotation.
     *
     * @param mainClass The main class of the application.
     * @since alpha.4
     */
    static void configure(@NonNull Class<?> mainClass) {
        JDABootConfiguration jdaBootConfiguration = mainClass.getAnnotation(JDABootConfiguration.class);
        if (jdaBootConfiguration == null) {
            jdaBootConfiguration = JDABoot.class.getAnnotation(JDABootConfiguration.class);
        }
        assert jdaBootConfiguration != null;
        applyConfiguration(jdaBootConfiguration);
    }

    /**
     * Applies the configuration specified by the {@link JDABootConfiguration} annotation.
     *
     * @param jdaBootConfiguration The AutoConfiguration annotation to apply.
     * @since alpha.4
     */
    private static void applyConfiguration(@NotNull JDABootConfiguration jdaBootConfiguration) {

        configProviderChain = (ConfigProviderChain) JDABootObjectManager.getOrInitialiseObject(ConfigProviderChain.class);
        for (Class<? extends ConfigProvider> configProvider : jdaBootConfiguration.additionalConfigProviders()) {
            configProviderChain.addConfigProviderToChain((ConfigProvider) JDABootObjectManager.getOrInitialiseObject(configProvider));
        }

        String configProfile = JDABoot.getStartupArgs().containsKey("profile") ? JDABoot.getStartupArgs().get("profile") : configProviderChain.getString("profile", jdaBootConfiguration.configProfile());
        log.info("Using configuration profile: '{}'", configProfile);
        configProviderChain.setConfigProfile(configProfile);

        try {
            translationProvider = jdaBootConfiguration.translationProvider().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ObjectInitializationException("Failed to instantiate translation provider", jdaBootConfiguration.translationProvider(), e);
        }

        intents = List.of(jdaBootConfiguration.intents());
        enabledCacheFlags = List.of(jdaBootConfiguration.enabledCacheFlags());
        disabledCacheFlags = List.of(jdaBootConfiguration.disabledCacheFlags());
        memberCachePolicy = jdaBootConfiguration.memberCachePolicy().getJDAUtilsMemberCachePolicy();

        consoleCommandsEnabled = jdaBootConfiguration.enableConsoleCommands();
    }

    /**
     * Initializes various managers.
     *
     * @param mainClass The main class of the application.
     * @param jda       The JDA instance.
     * @since alpha.4
     */
    static void initialiseManagers(@NonNull Class<?> mainClass, @NonNull ShardManager shardManager) {
        commandManager = new CommandManager(mainClass, shardManager);
        buttonManager = new ButtonManager(mainClass, shardManager);
        selectMenuManager = new SelectMenuManager(mainClass, shardManager);
        modalManager = new ModalManager(mainClass, shardManager);
        componentV2Manager = new ComponentV2Manager(mainClass);

        new EventManager(mainClass, shardManager);
        //noinspection InstantiationOfUtilityClass
        new EmbedManager(mainClass);
        new SchedulerManager(mainClass);

        if (consoleCommandsEnabled) {
            new ConsoleCommandManager(mainClass);
        }
        new ConfigValueManager(mainClass);
    }

    /**
     * Initializes global variables that provide dynamic values from the JDA instance.
     * These variables can be accessed at runtime and always reflect the current state.
     *
     * @param jda The JDA instance from which the values are retrieved.
     * @since 1.0.0-beta.1
     */
    static void initialiseGlobalVariables(@NonNull ShardManager shardManager, @NonNull JDA jda) {
        GlobalVariables.setDynamicValue("guildCount", () -> Integer.toString(shardManager.getGuilds().size()));
        GlobalVariables.setDynamicValue("selfUsername", () -> jda.getSelfUser().getName());
        GlobalVariables.setDynamicValue("shardCount", () -> String.valueOf(shardManager.getShardsTotal()));
    }

    static @NonNull CommandManager getCommandManager() {
        if (commandManager == null) {
            throw new StillInitializingException();
        }
        return commandManager;
    }

    public static @NonNull ConfigProviderChain getConfigProviderChain() {
        if (configProviderChain == null) {
            throw new StillInitializingException();
        }
        return configProviderChain;
    }

    static @NonNull List<@NonNull CacheFlag> getEnabledCacheFlags() {
        if (enabledCacheFlags == null) {
            throw new StillInitializingException();
        }
        return enabledCacheFlags;
    }

    static @NonNull List<@NonNull CacheFlag> getDisabledCacheFlags() {
        if (disabledCacheFlags == null) {
            throw new StillInitializingException();
        }
        return disabledCacheFlags;
    }

    static @NonNull List<@NonNull GatewayIntent> getIntents() {
        if (intents == null) {
            throw new StillInitializingException();
        }
        return intents;
    }

    public static @NonNull TranslationProvider getTranslationProvider() {
        if (translationProvider == null) {
            throw new StillInitializingException();
        }
        return translationProvider;
    }
}
