package de.jonafaust.jdaboot;


import de.jonafaust.jdaboot.command.CommandManager;
import de.jonafaust.jdaboot.configuration.Config;
import de.jonafaust.jdaboot.embeds.EmbedManager;
import de.jonafaust.jdaboot.event.EventManager;
import de.jonafaust.jdaboot.variables.VariableProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;

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

    @Getter
    private VariableProcessor variableProcessor;


    protected JDABoot(Class<?> mainClass) {
        this.config = Config.getInstance();
        this.mainClass = mainClass;
        init();
    }

    public static void run(Class<?> mainClass) {
        new JDABoot(mainClass);
    }

    public static JDABoot getInstance() {
        return instance;
    }

    private void init() {

        instance = this;

        try {
            discordLogin();
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

    private void discordLogin() throws InterruptedException, InvalidTokenException {
        log.info("Logging in to Discord...");
        this.builder = JDABuilder.createDefault(config.getString("discord.token"));
        this.jda = builder.build();
        this.variableProcessor = new VariableProcessor();
        this.commandHandler = new CommandManager(jda, mainClass);
        this.eventHandler = new EventManager(jda, mainClass);
        this.embedManager = new EmbedManager(mainClass);
        jda.awaitReady();
    }
}
