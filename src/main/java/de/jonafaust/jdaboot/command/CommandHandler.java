package de.jonafaust.jdaboot.command;

import de.jonafaust.jdaboot.annotation.Command;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

@Slf4j
public class CommandHandler extends ListenerAdapter {

    Reflections reflections;
    HashMap<String, SimpleCommand> commands = new HashMap<>();

    JDA jda;

    public CommandHandler(JDA jda, Class<?> mainClass) {
        this.jda = jda;
        this.reflections = new Reflections(mainClass.getPackageName().split("\\.")[0]);

        this.reflections.getTypesAnnotatedWith(Command.class).forEach(clazz -> {

            try {
                Command annotation = clazz.getAnnotation(Command.class);

                String name = clazz.getAnnotation(Command.class).name();
                String description = clazz.getAnnotation(Command.class).description();

                if(SimpleCommand.class.isAssignableFrom(clazz)) {
                    Constructor<?> constructor = clazz.getConstructor();
                    SimpleCommand cmd = (SimpleCommand) constructor.newInstance();

                    SlashCommandData data = cmd.onEnable(Commands.slash(name, description));

                    commands.put(data.getName(), cmd);
                    jda.upsertCommand(data).queue();
                    log.info("Registered command: " + data.getName());
                } else {
                    log.warn("Command " + name + " is not a child of SimpleCommand or OptionCommand! Skipping...");
                }




            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                e.printStackTrace();
            }

        });

        jda.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        String name = event.getName();
        SimpleCommand executor = commands.get(name);
        if (executor != null) {
            executor.onCommand((CommandContext) event);
        }
    }
}
