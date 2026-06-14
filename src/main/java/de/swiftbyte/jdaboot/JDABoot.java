package de.swiftbyte.jdaboot;


import de.swiftbyte.jdaboot.annotation.JDABootConfiguration;
import de.swiftbyte.jdaboot.configuration.ConfigProvider;
import de.swiftbyte.jdaboot.exceptions.JDABootInitializationException;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * The JDABoot class is responsible for initializing and starting the Discord bot.
 * It manages the bot's configuration, command handling, event handling, and more.
 *
 * @since alpha.4
 */
@JDABootConfiguration
@CustomLog
public final class JDABoot {

    @Getter
    private static @NonNull JDABoot instance;

    @Getter(AccessLevel.PROTECTED)
    private static @NonNull HashMap<@NonNull String, @NonNull String> startupArgs = new HashMap<>();

    private @NonNull Class<?> mainClass;

    private @Nullable ShardManager shardManager;

    @Getter
    private boolean isReady = false;

    private int minShardId = 0;

    private @NonNull ConfigProvider configProvider;

    /**
     * Protected constructor for JDABoot. Initializes the bot with the specified settings.
     *
     * @param mainClass The main class of your project.
     * @param args      The command line arguments.
     * @since alpha.4
     */
    private JDABoot(@NonNull Class<?> mainClass, @NonNull String @NonNull [] args) {
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
    public static void run(@NonNull Class<?> mainClass, @NonNull String @NonNull [] args) {
        try {
            new JDABoot(mainClass, args);
        } catch (Exception e) {
            log.error("An error occurred while starting jda-boot. The system will now exit", e);
            System.exit(1);
        }
    }

    /**
     * Private method to initialize the bot.
     *
     * @param args The command line arguments.
     * @since alpha.4
     */
    private void init(@NonNull String @NonNull [] args) {

        instance = this;

        //TODO improve arg parsing, maybe with a library
        for (String arg : args) {
            String[] split = arg.replace("-", "").split("=");
            if (split.length == 2) {
                startupArgs.put(Objects.requireNonNull(split[0]), Objects.requireNonNull(split[1]));
            } else {
                startupArgs.put(arg, arg);
            }
        }

        JDABootConfigurationManager.configure(mainClass);

        configProvider = JDABootConfigurationManager.getConfigProviderChain();

        try {
            login();
        } catch (InterruptedException e) {
            throw new JDABootInitializationException("Error while logging in to Discord", e);
        } catch (InvalidTokenException e) {
            throw new JDABootInitializationException("There is an invalid token provided in the config. You can create a token here: https://discord.com/developers/applications", e);
        } catch (Exception e) {
            throw new JDABootInitializationException("An error occurred while logging in to Discord!", e);
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
    private void login() throws InterruptedException, InvalidTokenException {
        log.info("Logging in to Discord...");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(configProvider.getString("discord.token"));

        if(!configProvider.getBoolean("sharding.enabled", false)) {
            builder.setShardsTotal(1);
        } else {

            int totalShards = configProvider.getInt("sharding.totalShards", 1);
            minShardId = configProvider.getInt("sharding.minShardId", 0);
            int maxShardId = configProvider.getInt("sharding.maxShardId", totalShards - 1);

            log.info("Sharding is enabled. Total shards: {}, Min shard ID: {}, Max shard ID: {}", totalShards, minShardId, maxShardId);

            builder.setShardsTotal(totalShards);
            builder.setShards(minShardId, maxShardId);
        }

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
        if (!allow.contains(GatewayIntent.GUILD_EXPRESSIONS)) {
            builder.disableCache(CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SOUNDBOARD_SOUNDS);
        }
        if (!allow.contains(GatewayIntent.SCHEDULED_EVENTS)) {
            builder.disableCache(CacheFlag.SCHEDULED_EVENTS);
        }

        builder.setMemberCachePolicy(JDABootConfigurationManager.getMemberCachePolicy());

        if (allow.isEmpty()) {
            builder.setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.DEFAULT));
        } else {
            builder.setEnabledIntents(allow);
        }

        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.getName().equalsIgnoreCase("getVoiceDispatchInterceptor")) {
                if (VoiceDispatchInterceptor.class.isAssignableFrom(declaredMethod.getReturnType())) {
                    builder.setVoiceDispatchInterceptor((VoiceDispatchInterceptor) JDABootObjectManager.runMethod(mainClass, declaredMethod));
                }
            } else if (declaredMethod.getName().equalsIgnoreCase("getAudioModuleConfig")) {
                if (AudioModuleConfig.class.isAssignableFrom(declaredMethod.getReturnType())) {
                    builder.setAudioModuleConfig((AudioModuleConfig) JDABootObjectManager.runMethod(mainClass, declaredMethod));
                }
            }
        }

        shardManager = builder.build();
        JDABootConfigurationManager.initialiseManagers(mainClass, shardManager, getFirstJDA());
        JDABootConfigurationManager.initialiseGlobalVariables(shardManager, getFirstJDA());

        awaitReady();

        isReady = true;

        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.getName().equalsIgnoreCase("onReady")) {
                JDABootObjectManager.runMethod(mainClass, declaredMethod);
            }
        }
    }

    private void awaitReady() throws InterruptedException {
        for (JDA jda : getShardManager().getShards()) {
            jda.awaitReady();
        }
    }

    /**
     * Updates the bot's commands.
     *
     * @see JDA#updateCommands()
     * @since alpha.2
     */
    public void updateCommands() {
        getFirstJDA().updateCommands().addCommands(JDABootConfigurationManager.getCommandManager().getGlobalData().values()).queue();
    }

    /**
     * Updates the commands for a specific guild.
     *
     * @param guildId The ID of the guild to update commands for.
     * @return true if the guild was found and the update was initiated, false otherwise.
     * @see Guild#updateCommands()
     * @since alpha.2
     */
    public boolean updateCommands(@NonNull String guildId) {
        Guild guild = getGuildById(guildId);
        if (guild == null) {
            return false;
        }
        guild.updateCommands().addCommands(JDABootConfigurationManager.getCommandManager().getGlobalData().values()).queue();
        return true;
    }

    /**
     * Registers a command for a specific guild.
     *
     * @param guildId   The ID of the guild to register the command for.
     * @param commandId The ID of the command to register.
     * @return true if the guild was found and the update was initiated, false otherwise.
     * @see Guild#upsertCommand(CommandData)
     * @since alpha.2
     */
    public boolean registerCommand(@NonNull String guildId, @NonNull String commandId) {
        Guild guild = getGuildById(guildId);
        CommandData commandData = JDABootConfigurationManager.getCommandManager().getCommandData().get(commandId);
        if (guild == null || commandData == null) {
            return false;
        }
        guild.upsertCommand(commandData).queue();
        return true;
    }

    public @Nullable Guild getGuildById(String id) {
        return getShardManager().getGuildById(id);
    }

    public @NonNull ShardManager getShardManager() {
        if (shardManager == null) {
            throw new IllegalStateException("ShardManager is not initialized yet");
        }
        return shardManager;
    }

    public @NonNull JDA getFirstJDA() {
        JDA jda = getShardManager().getShardById(minShardId);
        if (jda == null) throw new IllegalStateException("No JDA instance found for min shard ID " + minShardId);
        return jda;
    }
}
