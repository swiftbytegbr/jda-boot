package de.swiftbyte.jdaboot;


import de.swiftbyte.jdaboot.annotation.JDABootConfiguration;
import de.swiftbyte.jdaboot.configuration.ConfigProvider;
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

    @Getter
    private JDA jda;

    private Class<?> mainClass;

    private ConfigProvider configProvider;

    /**
     * Protected constructor for JDABoot. Initializes the bot with the specified settings.
     *
     * @param mainClass The main class of your project.
     * @since alpha.4
     */
    protected JDABoot(Class<?> mainClass) {
        this.mainClass = mainClass;
        init(null, null);
    }

    /**
     * Protected constructor for JDABoot. Initializes the bot with the specified settings.
     *
     * @param mainClass         The main class of your project.
     * @param memberCachePolicy The policy to use for member caching.
     * @param allow             The list of allowed GatewayIntents.
     * @since alpha.4
     */
    protected JDABoot(Class<?> mainClass, MemberCachePolicy memberCachePolicy, List<GatewayIntent> allow) {
        this.mainClass = mainClass;
        init(memberCachePolicy, allow);
    }

    /**
     * Protected constructor for JDABoot. Initializes the bot with the specified settings.
     *
     * @param properties The settings to use when starting the bot.
     * @since alpha.4
     */
    protected JDABoot(Class<?> mainClass, DiscordSettings properties) {
        this.mainClass = mainClass;
        init(properties.memberCachePolicy, properties.intents);
    }

    /**
     * Starts the Discord bot with the specified settings.
     *
     * @since alpha.4
     */
    public static void run(Class<?> mainClass) {
        new JDABoot(mainClass);
    }

    /**
     * Starts the Discord bot with the specified settings.
     *
     * @param settings The settings to use when starting the bot.
     * @since alpha.4
     * @deprecated Use {@link #run(Class)} instead and configure via {@link JDABootConfiguration}.
     */
    @Deprecated(forRemoval = true)
    public static void run(Class<?> mainClass, DiscordSettings settings) {
        new JDABoot(mainClass, settings);
    }

    /**
     * This method starts the Discord bot with the specified information.
     * For advanced configuration see {@link #run(Class, DiscordSettings)}.
     *
     * @param mainClass         The main class of your project.
     * @param memberCachePolicy The policy to use for member caching.
     * @param allow             The list of allowed GatewayIntents.
     * @since alpha.4
     * @deprecated Use {@link #run(Class)} instead and configure via {@link JDABootConfiguration}.
     */
    @Deprecated(forRemoval = true)
    public static void run(Class<?> mainClass, MemberCachePolicy memberCachePolicy, GatewayIntent... allow) {
        new JDABoot(mainClass, memberCachePolicy, List.of(allow));
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
     * @param memberCachePolicy The policy to use for member caching.
     * @param allow             The list of allowed GatewayIntents.
     * @since alpha.4
     */
    private void init(MemberCachePolicy memberCachePolicy, List<GatewayIntent> allow) {

        instance = this;

        JDABootConfigurationManager.autoConfigure(mainClass);

        configProvider = JDABootConfigurationManager.getConfigProvider();

        try {
            discordLogin(memberCachePolicy, allow);
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
     * @param memberCachePolicy The policy to use for member caching.
     * @param allow             The list of allowed GatewayIntents.
     * @throws InterruptedException  If the login process is interrupted.
     * @throws InvalidTokenException If the provided token is invalid.
     * @since alpha.4
     */
    private void discordLogin(MemberCachePolicy memberCachePolicy, List<GatewayIntent> allow) throws InterruptedException, InvalidTokenException {
        log.info("Logging in to Discord...");
        JDABuilder builder = JDABuilder.createDefault(configProvider.getString("discord.token"));

        if (allow == null) allow = JDABootConfigurationManager.getIntents();

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

        if (memberCachePolicy != null) builder.setMemberCachePolicy(memberCachePolicy);
        else builder.setMemberCachePolicy(JDABootConfigurationManager.getMemberCachePolicy());

        if (allow.isEmpty()) builder.setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.DEFAULT));
        else builder.setEnabledIntents(allow);

        this.jda = builder.build();
        JDABootConfigurationManager.initialiseManagers(mainClass, jda);
        jda.awaitReady();
    }

    /**
     * The DiscordSettings class is used for advanced configuration of JDA-Boot.
     *
     * @since alpha.4
     */
    @Builder
    public static class DiscordSettings {

        /**
         * The policy to use for member caching.
         */
        private MemberCachePolicy memberCachePolicy;

        /**
         * The list of GatewayIntents to use.
         */
        private List<GatewayIntent> intents;
    }
}
