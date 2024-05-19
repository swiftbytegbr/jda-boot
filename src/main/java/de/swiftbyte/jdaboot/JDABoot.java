package de.swiftbyte.jdaboot;


import de.swiftbyte.jdaboot.annotation.JDABootConfiguration;
import de.swiftbyte.jdaboot.configuration.ConfigProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * The JDABoot class is responsible for initializing and starting the Discord bot.
 * It manages the bot's configuration, command handling, event handling, and more.
 *
 * @since alpha.4
 */
@Slf4j
@JDABootConfiguration
public class JDABoot {

    @Getter
    private static JDABoot instance;

    @Getter(AccessLevel.PROTECTED)
    private static HashMap<String, String> startupArgs;

    @Getter
    private JDA jda;

    private Class<?> mainClass;

    private ConfigProvider configProvider;

    /**
     * Protected constructor for JDABoot. Initializes the bot with the specified settings.
     *
     * @param mainClass The main class of your project.
     * @param args      The command line arguments.
     * @since alpha.4
     */
    protected JDABoot(Class<?> mainClass, String[] args) {
        this.mainClass = mainClass;
        init(args);
    }

    /**
     * Starts the Discord bot with the specified settings.
     *
     * @param mainClass The main class of your project.
     * @param args      The command line arguments.
     * @since alpha.4
     */
    public static void run(Class<?> mainClass, String[] args) {
        new JDABoot(mainClass, args);
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
     * @param guildId   The ID of the guild to register the command for.
     * @param commandId The ID of the command to register.
     * @see Guild#upsertCommand(CommandData)
     * @since alpha.2
     */
    public void registerCommand(String guildId, String commandId) {
        jda.getGuildById(guildId).upsertCommand(JDABootConfigurationManager.getCommandManager().getCommandData().get(commandId)).queue();
    }

    /**
     * Private method to initialize the bot.
     *
     * @param args The command line arguments.
     * @since alpha.4
     */
    private void init(String[] args) {

        instance = this;
        startupArgs = new HashMap<>();
        for (String arg : args) {
            String[] split = arg.replace("-", "").split("=");
            if (split.length == 2) {
                startupArgs.put(split[0], split[1]);
            } else {
                startupArgs.put(arg, arg);
            }
        }

        JDABootConfigurationManager.configure(mainClass);

        configProvider = JDABootConfigurationManager.getConfigProviderChain();

        try {
            discordLogin();
        } catch (InterruptedException e) {
            log.error("Error while logging in to Discord. " + "\nThe system will now exit.", e);
            System.exit(1);
        } catch (InvalidTokenException e) {
            log.error("There is an invalid token in the config provided. You can create a token here: https://discord.com/developers/applications", e);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            log.error("An error occurred while logging in to Discord!", e);
            System.exit(1);
        }
        log.info("JDABoot initialized!");
    }

    /**
     * Private method to log in to Discord.
     *
     * @throws InterruptedException  If the login process is interrupted.
     * @throws InvalidTokenException If the provided token is invalid.
     * @since alpha.4
     */
    private void discordLogin() throws InterruptedException, InvalidTokenException {
        log.info("Logging in to Discord...");
        JDABuilder builder = JDABuilder.createDefault(configProvider.getString("discord.token"));

        List<GatewayIntent> allow = JDABootConfigurationManager.getIntents();

        List<CacheFlag> disabledCacheFlags = JDABootConfigurationManager.getDisabledCacheFlags();

        for (CacheFlag cacheFlag : JDABootConfigurationManager.getEnabledCacheFlags()) {
            builder.enableCache(cacheFlag);
        }

        for (CacheFlag cacheFlag : disabledCacheFlags) {
            builder.disableCache(cacheFlag);
        }

        if (!allow.contains(GatewayIntent.GUILD_VOICE_STATES)) {
            builder.disableCache(CacheFlag.VOICE_STATE);
        }
        if (!allow.contains(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)) {
            builder.disableCache(CacheFlag.EMOJI, CacheFlag.STICKER);
        }
        if (!allow.contains(GatewayIntent.SCHEDULED_EVENTS)) {
            builder.disableCache(CacheFlag.SCHEDULED_EVENTS);
        }

        builder.setMemberCachePolicy(JDABootConfigurationManager.getMemberCachePolicy());

        if (allow.isEmpty()) builder.setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.DEFAULT));
        else builder.setEnabledIntents(allow);

        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.getName().equalsIgnoreCase("getVoiceDispatchInterceptor")) {
                if (VoiceDispatchInterceptor.class.isAssignableFrom(declaredMethod.getReturnType()))
                    builder.setVoiceDispatchInterceptor((VoiceDispatchInterceptor) JDABootObjectManager.runMethod(mainClass, declaredMethod));
            }
        }

        this.jda = builder.build();
        JDABootConfigurationManager.initialiseManagers(mainClass, jda);
        jda.awaitReady();

        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.getName().equalsIgnoreCase("onReady")) {
                JDABootObjectManager.runMethod(mainClass, declaredMethod);
            }
        }
    }
}
