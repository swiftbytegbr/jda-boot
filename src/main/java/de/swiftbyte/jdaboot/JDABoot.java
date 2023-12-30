package de.swiftbyte.jdaboot;


import de.swiftbyte.jdaboot.button.ButtonManager;
import de.swiftbyte.jdaboot.command.CommandManager;
import de.swiftbyte.jdaboot.configuration.Config;
import de.swiftbyte.jdaboot.configuration.PropertiesConfig;
import de.swiftbyte.jdaboot.embeds.EmbedManager;
import de.swiftbyte.jdaboot.event.EventManager;
import de.swiftbyte.jdaboot.variables.*;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;
import java.util.function.Supplier;

/**
 * The JDABoot class is responsible for initializing and starting the Discord bot.
 * It manages the bot's configuration, command handling, event handling, and more.
 *
 * @since alpha.4
 */
@Slf4j
public class JDABoot {

    @Getter
    private static JDABoot instance;

    @Getter
    private JDA jda;
    private JDABuilder builder;

    @Getter
    private Config config;
    private Class<?> mainClass;

    @Getter
    private Supplier<TranslationBundle> translationProvider;
    private CommandManager commandHandler;
    private EventManager eventHandler;
    private EmbedManager embedManager;
    private TranslatorManager translatorManager;

    @Getter
    private ButtonManager buttonManager;

    @Getter
    private VariableProcessor variableProcessor;

    @Getter
    private Translator translator;

    /**
     * Protected constructor for JDABoot. Initializes the bot with the specified settings.
     *
     * @param mainClass The main class of your project.
     * @param memberCachePolicy The policy to use for member caching.
     * @param allow The list of allowed GatewayIntents.
     * @param translationProvider The custom translation provider to use.
     * @since alpha.4
     */
    protected JDABoot(Class<?> mainClass, MemberCachePolicy memberCachePolicy, List<GatewayIntent> allow,
                      Supplier<TranslationBundle> translationProvider) {
        this.config = PropertiesConfig.getInstance();
        this.mainClass = mainClass;
        this.translationProvider = translationProvider;
        init(memberCachePolicy, allow);
    }

    /**
     * Protected constructor for JDABoot. Initializes the bot with the specified settings.
     *
     * @param properties The settings to use when starting the bot.
     * @since alpha.4
     */
    protected JDABoot(JDABoot.Settings properties) {
        this.config = properties.configProvider.get();
        this.mainClass = properties.mainClass;
        this.translationProvider = properties.translationBundleProvider;
        init(properties.memberCachePolicy, properties.intents);
    }

    /**
     * Starts the Discord bot with the specified settings.
     *
     * @param settings The settings to use when starting the bot.
     * @since alpha.4
     */
    public static void run(JDABoot.Settings settings) {
        new JDABoot(settings);
    }

    /**
     * This method starts the Discord bot with the specified information.
     * For advanced configuration see {@link #run(Settings)}.
     *
     * @param mainClass The main class of your project.
     * @param memberCachePolicy The policy to use for member caching.
     * @param allow The list of allowed GatewayIntents.
     * @since alpha.4
     */
    public static void run(Class<?> mainClass, MemberCachePolicy memberCachePolicy, GatewayIntent... allow) {
        new JDABoot(mainClass, memberCachePolicy, List.of(allow), ResourceTranslationBundle::new);
    }

    /**
     * This method starts the Discord bot with the specified information.
     *
     * @param mainClass The main class of your project.
     * @param memberCachePolicy The policy to use for member caching.
     * @param translationProvider The custom translation provider to use.
     * @param allow The list of allowed GatewayIntents.
     * @deprecated Use {@link #run(Settings)} instead.
     * @since alpha.4
     */
    @Deprecated(forRemoval = true)
    public static void run(Class<?> mainClass, MemberCachePolicy memberCachePolicy,
                           Supplier<TranslationBundle> translationProvider, GatewayIntent... allow) {
        new JDABoot(mainClass, memberCachePolicy, List.of(allow), translationProvider);
    }

    /**
     * Updates the bot's commands.
     *
     * @see JDA#updateCommands()
     * @since alpha.2
     */
    public void updateCommands() {
        jda.updateCommands().queue();
    }

    /**
     * Updates the commands for a specific guild.
     *
     * @param guildId The ID of the guild to update commands for.
     * @see Guild#updateCommands()
     * @since alpha.2
     */
    public void updateCommands(String guildId) {
        jda.getGuildById(guildId).updateCommands().queue();
    }

    /**
     * Registers a command for a specific guild.
     *
     * @param guildId The ID of the guild to register the command for.
     * @param commandId The ID of the command to register.
     * @see Guild#upsertCommand(CommandData)
     * @since alpha.2
     */
    public void registerCommand(String guildId, String commandId) {
        jda.getGuildById(guildId).upsertCommand(commandHandler.getCommandData().get(commandId)).queue();
    }

    /**
     * Private method to initialize the bot.
     *
     * @param memberCachePolicy The policy to use for member caching.
     * @param allow The list of allowed GatewayIntents.
     * @since alpha.4
     */
    private void init(MemberCachePolicy memberCachePolicy, List<GatewayIntent> allow) {

        instance = this;

        try {
            discordLogin(memberCachePolicy, allow);
        } catch (InterruptedException e) {
            log.error("Error while logging in to Discord. " + "\nThe system will no exit.");
            System.exit(1);
        } catch (InvalidTokenException | IllegalArgumentException e) {
            log.error("There is an invalid token in the config provided. You can create a token here: https://discord.com/developers/applications", e);
            System.exit(1);
        }
        log.info("JDABoot initialized!");
    }

    /**
     * Private method to log in to Discord.
     *
     * @param memberCachePolicy The policy to use for member caching.
     * @param allow The list of allowed GatewayIntents.
     * @throws InterruptedException If the login process is interrupted.
     * @throws InvalidTokenException If the provided token is invalid.
     * @since alpha.4
     */
    private void discordLogin(MemberCachePolicy memberCachePolicy, List<GatewayIntent> allow) throws InterruptedException, InvalidTokenException {
        log.info("Logging in to Discord...");
        this.builder = JDABuilder.createDefault(config.getString("discord.token"));

        if (!allow.contains(GatewayIntent.GUILD_VOICE_STATES)) {
            builder.disableCache(CacheFlag.VOICE_STATE);
        }
        if (!allow.contains(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)) {
            builder.disableCache(CacheFlag.EMOJI, CacheFlag.STICKER);
        }
        if (!allow.contains(GatewayIntent.SCHEDULED_EVENTS)) {
            builder.disableCache(CacheFlag.SCHEDULED_EVENTS);
        }

        builder.setMemberCachePolicy(memberCachePolicy);
        builder.setEnabledIntents(allow);

        this.jda = builder.build();
        this.translator = new Translator();
        this.variableProcessor = new VariableProcessor();
        this.translatorManager = new TranslatorManager(mainClass);
        this.commandHandler = new CommandManager(jda, mainClass);
        this.eventHandler = new EventManager(jda, mainClass);
        this.buttonManager = new ButtonManager(jda, mainClass);
        this.embedManager = new EmbedManager(mainClass);
        jda.awaitReady();
    }

    /**
     * The Settings class is used for advanced configuration of JDA-Boot.
     *
     * @since alpha.4
     */
    @Builder
    public static class Settings {

        /**
         * The main class of your project.
         */
        private Class<?> mainClass;

        /**
         * The policy to use for member caching.
         */
        private MemberCachePolicy memberCachePolicy;

        /**
         * The list of GatewayIntents to use.
         */
        private List<GatewayIntent> intents;

        /**
         * The provider to use for configuration.
         */
        @Builder.Default
        private Supplier<Config> configProvider = PropertiesConfig::getInstance;

        /**
         * The provider to use for translation bundles.
         */
        @Builder.Default
        private Supplier<TranslationBundle> translationBundleProvider = ResourceTranslationBundle::new;
    }
}
