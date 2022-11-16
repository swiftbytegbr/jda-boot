package de.jonafaust.jdaboot;


import de.jonafaust.jdaboot.command.CommandManager;
import de.jonafaust.jdaboot.configuration.Config;
import de.jonafaust.jdaboot.embeds.EmbedManager;
import de.jonafaust.jdaboot.event.EventManager;
import de.jonafaust.jdaboot.variables.Translator;
import de.jonafaust.jdaboot.variables.TranslatorManager;
import de.jonafaust.jdaboot.variables.VariableProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;

@Slf4j
public class JDABoot {

    private static JDABoot instance;
    @Getter
    private JDA jda;
    private JDABuilder builder;
    @Getter
    private Config config;
    private Class<?> mainClass;
    private CommandManager commandHandler;
    private EventManager eventHandler;
    private EmbedManager embedManager;
    private TranslatorManager translatorManager;

    @Getter
    private VariableProcessor variableProcessor;
    @Getter
    private Translator translator;


    protected JDABoot(Class<?> mainClass, MemberCachePolicy memberCachePolicy, List<GatewayIntent> allow) {
        this.config = Config.getInstance();
        this.mainClass = mainClass;
        init(memberCachePolicy, allow);
    }

    public static void run(Class<?> mainClass, MemberCachePolicy memberCachePolicy, GatewayIntent... allow) {
        new JDABoot(mainClass, memberCachePolicy, List.of(allow));
    }

    public static JDABoot getInstance() {
        return instance;
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
            log.error("There is an invalid token in the config provided. You can create a token here: https://discord.com/developers/applications");
            e.printStackTrace();
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
        this.embedManager = new EmbedManager(mainClass);
        jda.awaitReady();
    }
}
