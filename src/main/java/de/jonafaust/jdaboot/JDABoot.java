package de.jonafaust.jdaboot;


import de.jonafaust.jdaboot.command.CommandHandler;
import de.jonafaust.jdaboot.configuration.Config;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;

@Slf4j
public class JDABoot {

    @Getter
    private JDA jda;
    private JDABuilder builder;

    @Getter
    private Config config;

    private Class<?> mainClass;
    private CommandHandler commandHandler;

    private static JDABoot instance;


    protected JDABoot(Class<?> mainClass) {
        this.config = Config.getInstance();
        this.mainClass = mainClass;
        init();
    }


    private void init() {
        try {
            discordLogin();
        } catch (InterruptedException e) {
            log.error("Error while logging in to Discord. "  + "\nThe system will no exit.");
            System.exit(1);
        } catch (InvalidTokenException | IllegalArgumentException e) {
            log.error("There is an invalid token in the config provided. You can create a token here: https://discord.com/developers/applications");
            System.exit(1);
        }
        this.commandHandler = new CommandHandler(jda, mainClass);
        log.info("JDABoot initialized!");
    }

    private void discordLogin() throws InterruptedException, InvalidTokenException {
        log.info("Logging in to Discord...");
        this.builder = JDABuilder.createDefault(config.getString("discord.token"));
        this.jda = builder.build();
        jda.awaitReady();
        log.info("Logged in!");
    }

    public static void run(Class<?> mainClass) {
        new JDABoot(mainClass);
    }

    public static JDABoot getInstance() {
        return instance;
    }
}
