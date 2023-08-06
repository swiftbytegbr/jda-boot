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
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;
import java.util.function.Supplier;

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

    public static void run(JDABoot.Settings settings) {
        new JDABoot(settings);
    }

    public static void run(Class<?> mainClass, MemberCachePolicy memberCachePolicy, GatewayIntent... allow) {
        new JDABoot(mainClass, memberCachePolicy, List.of(allow), ResourceTranslationBundle::new);
    }

    /**
     * @deprecated Use run(JDABoot.Settings) instead
     */
    @Deprecated(forRemoval = true)
    public static void run(Class<?> mainClass, MemberCachePolicy memberCachePolicy,
                           Supplier<TranslationBundle> translationProvider, GatewayIntent... allow) {
        new JDABoot(mainClass, memberCachePolicy, List.of(allow), translationProvider);
    }


    public void updateCommands() {
        jda.updateCommands().queue();
    }

    public void updateCommands(String guildid) {
        jda.getGuildById(guildid).updateCommands().queue();
    }

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

    @Builder
    public static class Settings {
        private Class<?> mainClass;
        private MemberCachePolicy memberCachePolicy;
        private List<GatewayIntent> intents;

        @Builder.Default
        private Supplier<Config> configProvider = PropertiesConfig::getInstance;

        @Builder.Default
        private Supplier<TranslationBundle> translationBundleProvider = ResourceTranslationBundle::new;
    }
}
