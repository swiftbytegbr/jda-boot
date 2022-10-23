package de.jonafaust.jdaboot;


import de.jonafaust.jdaboot.command.CommandHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@Slf4j
public class JDABoot {

    @Getter
    private JDA jda;
    private JDABuilder builder;

    private Class<?> mainClass;
    private CommandHandler commandHandler;

    private static JDABoot instance;


    protected JDABoot(Class<?> mainClass) {
        this.builder = JDABuilder.createDefault("TOKEN FOR TESTING PURPOSES");
        this.jda = builder.build();
        this.mainClass = mainClass;
        this.commandHandler = new CommandHandler(jda, mainClass);
    }

    public static void run(Class<?> mainClass) {
        new JDABoot(mainClass);
    }

    public static JDABoot getInstance() {
        return instance;
    }
}
