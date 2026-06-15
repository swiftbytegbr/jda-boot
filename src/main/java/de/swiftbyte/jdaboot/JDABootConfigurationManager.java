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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
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

    @Getter(AccessLevel.PROTECTED)
    private static @NonNull List<@NonNull Class<? extends ShardManagerBuilderCustomizer>> builderCustomizers = new ArrayList<>();

    private static @Nullable List<@NonNull GatewayIntent> intents;

    private static @Nullable List<@NonNull CacheFlag> enabledCacheFlags;

    private static @Nullable List<@NonNull CacheFlag> disabledCacheFlags;

    /**
     * The member cache policy applied to every shard.
     */
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

    /**
     * The initialized button manager.
     */
    @Getter
    private static @Nullable ButtonManager buttonManager;

    /**
     * The initialized modal manager.
     */
    @Getter
    private static @Nullable ModalManager modalManager;

    /**
     * The initialized select menu manager.
     */
    @Getter
    private static @Nullable SelectMenuManager selectMenuManager;

    /**
     * The initialized Components V2 manager.
     */
    @Getter
    private static @Nullable ComponentV2Manager componentV2Manager;

    /**
     * The initialized scheduler manager.
     */
    @Getter
    private static SchedulerManager schedulerManager;

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
     * @param jdaBootConfiguration The JDABoot configuration annotation to apply.
     * @since alpha.4
     */
    private static void applyConfiguration(@NotNull JDABootConfiguration jdaBootConfiguration) {

        configProviderChain = (ConfigProviderChain) JDABootObjectManager.getOrInitialiseObject(ConfigProviderChain.class);
        for (Class<? extends ConfigProvider> configProvider : jdaBootConfiguration.additionalConfigProviders()) {
            configProviderChain.addConfigProviderToChain((ConfigProvider) JDABootObjectManager.getOrInitialiseObject(configProvider));
        }

        List<String> activeProfiles = resolveActiveProfiles(jdaBootConfiguration);
        log.info("Using configuration profiles: {}", activeProfiles);
        configProviderChain.setActiveProfiles(activeProfiles);

        try {
            translationProvider = jdaBootConfiguration.translationProvider().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ObjectInitializationException("Failed to instantiate translation provider", jdaBootConfiguration.translationProvider(), e);
        }

        builderCustomizers = List.of(jdaBootConfiguration.builderCustomizers());

        intents = List.of(jdaBootConfiguration.intents());
        enabledCacheFlags = List.of(jdaBootConfiguration.enabledCacheFlags());
        disabledCacheFlags = List.of(jdaBootConfiguration.disabledCacheFlags());
        memberCachePolicy = jdaBootConfiguration.memberCachePolicy().getJDAUtilsMemberCachePolicy();

        consoleCommandsEnabled = jdaBootConfiguration.enableConsoleCommands();
    }

    /**
     * Resolves the active configuration profiles from startup arguments, configuration, or annotation defaults.
     *
     * @param jdaBootConfiguration The framework configuration annotation.
     * @return The active profiles with {@code default} as the first entry.
     * @since 1.0.0-beta.2
     */
    private static @NonNull List<@NonNull String> resolveActiveProfiles(@NonNull JDABootConfiguration jdaBootConfiguration) {
        Object configuredProfiles;
        if (JDABoot.getStartupArgs().containsKey("profiles")) {
            configuredProfiles = JDABoot.getStartupArgs().get("profiles");
        } else if (getConfigProviderChain().hasKey("profiles")) {
            configuredProfiles = getConfigProviderChain().get("profiles", "");
        } else {
            configuredProfiles = List.of(jdaBootConfiguration.configProfiles());
        }

        LinkedHashSet<String> profiles = new LinkedHashSet<>();
        profiles.add("default");

        if (configuredProfiles instanceof Collection<?> configuredProfileList) {
            for (Object configuredProfile : configuredProfileList) {
                addProfiles(profiles, String.valueOf(configuredProfile));
            }
        } else {
            addProfiles(profiles, String.valueOf(configuredProfiles));
        }

        return List.copyOf(profiles);
    }

    /**
     * Adds comma-separated profile names to the active profile set.
     *
     * @param profiles           The active profile set.
     * @param configuredProfiles The configured profile names.
     * @since 1.0.0-beta.2
     */
    private static void addProfiles(@NonNull LinkedHashSet<String> profiles,
                                    @NonNull String configuredProfiles) {
        for (String profile : configuredProfiles.split(",")) {
            String normalizedProfile = profile.trim();
            if (!normalizedProfile.isEmpty() && !normalizedProfile.equals("default")) {
                profiles.add(normalizedProfile);
            }
        }
    }

    /**
     * Initializes framework managers and registers their listeners with every shard.
     *
     * @param mainClass    The main class of the application.
     * @param shardManager The shard manager used for Discord event handling.
     * @since alpha.4
     */
    static void initialiseManagers(@NonNull Class<?> mainClass, @NonNull ShardManager shardManager) {
        commandManager = new CommandManager(mainClass, shardManager);
        buttonManager = new ButtonManager(mainClass, shardManager);
        selectMenuManager = new SelectMenuManager(mainClass, shardManager);
        modalManager = new ModalManager(mainClass, shardManager);
        componentV2Manager = new ComponentV2Manager(mainClass);

        int schedulerThreadPoolSize = getConfigProviderChain().getInt("scheduler.threadPoolSize", 5);
        if (schedulerThreadPoolSize <= 0) {
            log.warn("Invalid scheduler thread pool size: {}. Using default value of 5.", schedulerThreadPoolSize);
            schedulerThreadPoolSize = 5;
        }
        schedulerManager = new SchedulerManager(mainClass, schedulerThreadPoolSize);

        new EventManager(mainClass, shardManager);
        //noinspection InstantiationOfUtilityClass
        new EmbedManager(mainClass);

        if (consoleCommandsEnabled) {
            new ConsoleCommandManager(mainClass);
        }
        new ConfigValueManager(mainClass);
    }

    /**
     * Initializes global variables that provide dynamic values from the shard manager.
     * These variables can be accessed at runtime and always reflect the current state.
     *
     * @param shardManager The shard manager used to determine guild and shard counts.
     * @param jda          A JDA instance used to retrieve information shared by all shards.
     * @since 1.0.0-beta.1
     */
    static void initialiseGlobalVariables(@NonNull ShardManager shardManager, @NonNull JDA jda) {
        GlobalVariables.setDynamicValue("guildCount", () -> Integer.toString(shardManager.getGuilds().size()));
        GlobalVariables.setDynamicValue("selfUsername", () -> jda.getSelfUser().getName());
        GlobalVariables.setDynamicValue("shardCount", () -> String.valueOf(shardManager.getShardsTotal()));
    }

    /**
     * Returns the initialized command manager.
     *
     * @return The command manager.
     * @throws StillInitializingException If the manager has not been initialized yet.
     */
    public static @NonNull CommandManager getCommandManager() {
        if (commandManager == null) {
            throw new StillInitializingException();
        }
        return commandManager;
    }

    /**
     * Returns the configured provider chain.
     *
     * @return The configuration provider chain.
     * @throws StillInitializingException If configuration has not been initialized yet.
     */
    public static @NonNull ConfigProviderChain getConfigProviderChain() {
        if (configProviderChain == null) {
            throw new StillInitializingException();
        }
        return configProviderChain;
    }

    /**
     * Returns the cache flags that should be enabled.
     *
     * @return The enabled cache flags.
     * @throws StillInitializingException If configuration has not been initialized yet.
     */
    static @NonNull List<@NonNull CacheFlag> getEnabledCacheFlags() {
        if (enabledCacheFlags == null) {
            throw new StillInitializingException();
        }
        return enabledCacheFlags;
    }

    /**
     * Returns the cache flags that should be disabled.
     *
     * @return The disabled cache flags.
     * @throws StillInitializingException If configuration has not been initialized yet.
     */
    static @NonNull List<@NonNull CacheFlag> getDisabledCacheFlags() {
        if (disabledCacheFlags == null) {
            throw new StillInitializingException();
        }
        return disabledCacheFlags;
    }

    /**
     * Returns the configured gateway intents.
     *
     * @return The gateway intents.
     * @throws StillInitializingException If configuration has not been initialized yet.
     */
    static @NonNull List<@NonNull GatewayIntent> getIntents() {
        if (intents == null) {
            throw new StillInitializingException();
        }
        return intents;
    }

    /**
     * Returns the configured translation provider.
     *
     * @return The translation provider.
     * @throws StillInitializingException If configuration has not been initialized yet.
     */
    public static @NonNull TranslationProvider getTranslationProvider() {
        if (translationProvider == null) {
            throw new StillInitializingException();
        }
        return translationProvider;
    }


}
