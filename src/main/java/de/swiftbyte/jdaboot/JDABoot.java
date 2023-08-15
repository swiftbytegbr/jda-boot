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
 * This class is for initializing JDA Boot and starting the Discord bot.
 *
 * @since alpha.1
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


    protected JDABoot(Class<?> mainClass, MemberCachePolicy memberCachePolicy, List<GatewayIntent> allow,
                      Supplier<TranslationBundle> translationProvider) {
        this.config = PropertiesConfig.getInstance();
        this.mainClass = mainClass;
        this.translationProvider = translationProvider;
        init(memberCachePolicy, allow);
    }

    protected JDABoot(JDABoot.Settings properties) {
        this.config = properties.configProvider.get();
        this.mainClass = properties.mainClass;
        this.translationProvider = properties.translationBundleProvider;
        init(properties.memberCachePolicy, properties.intents);
    }

    /**
     * This method starts the Discord bot with the specified information.
     *
     * @since alpha.3.5
     * @param settings the {@link Settings} object to use
     */
    public static void run(JDABoot.Settings settings) {
        new JDABoot(settings);
    }

    /**
     * This method starts the Discord bot with the specified information.
     * For advanced configuration see {@link #run(Settings)}.
     *
     * @since alpha.1
     * @param mainClass the main class of your project
     * @param memberCachePolicy the {@link MemberCachePolicy} to use
     * @param allow the list of allowed {@link GatewayIntent} to use
     */
    public static void run(Class<?> mainClass, MemberCachePolicy memberCachePolicy, GatewayIntent... allow) {
        new JDABoot(mainClass, memberCachePolicy, List.of(allow), ResourceTranslationBundle::new);
    }

    /**
     * This method starts the Discord bot with the specified information.
     *
     * @since alpha.1
     * @param mainClass the main class of your project
     * @param memberCachePolicy the {@link MemberCachePolicy} to use
     * @param translationProvider the custom translation provider to use
     * @param allow the list of allowed {@link GatewayIntent} to use
     * @deprecated Use {@link #run(Settings)} instead
     */
    @Deprecated(forRemoval = true)
    public static void run(Class<?> mainClass, MemberCachePolicy memberCachePolicy,
                           Supplier<TranslationBundle> translationProvider, GatewayIntent... allow) {
        new JDABoot(mainClass, memberCachePolicy, List.of(allow), translationProvider);
    }

    /**
     * @see JDA#updateCommands()
     * @since alpha.2
     */
    public void updateCommands() {
        jda.updateCommands().queue();
    }

    /**
     * @see Guild#updateCommands()
     * @since alpha.2
     * @param guildId the id of the guild to use
     */
    public void updateCommands(String guildId) {
        jda.getGuildById(guildId).updateCommands().queue();
    }

    /**
     * @see Guild#upsertCommand(CommandData) 
     * @since alpha.2
     * @param guildId the id of the guild to use
     * @param commandId the jda-boot command id to use
     */
    public void registerCommand(String guildId, String commandId) {
        jda.getGuildById(guildId).upsertCommand(commandHandler.getCommandData().get(commandId)).queue();
    }

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
     * This class is used for advanced configuration of JDA-Boot.
     *
     * @since alpha.3.5
     */
    @Builder
    public static class Settings {

        /**
         * The main class of your project.
         */
        private Class<?> mainClass;

        /**
         * The {@link MemberCachePolicy} to use.
         */
        private MemberCachePolicy memberCachePolicy;

        /**
         * The list of {@link GatewayIntent} to use.
         */
        private List<GatewayIntent> intents;

        /**
         * The config provider to use.
         */
        @Builder.Default
        private Supplier<Config> configProvider = PropertiesConfig::getInstance;

        /**
         * The translation bundle provider to use.
         */
        @Builder.Default
        private Supplier<TranslationBundle> translationBundleProvider = ResourceTranslationBundle::new;
    }
}
