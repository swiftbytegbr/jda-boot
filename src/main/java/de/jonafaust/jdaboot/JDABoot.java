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
        this.builder = JDABuilder.createDefault("OTIzMzM5MzgyNDQwNzUxMTc2.GJRr0K.g2g1_g7doo45enqkyBsM7Q6p54KxDCZRcz24S4");
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
