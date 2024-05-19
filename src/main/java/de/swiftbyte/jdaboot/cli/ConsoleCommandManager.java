package de.swiftbyte.jdaboot.cli;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.cli.ConsoleCommandDefinition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.HashMap;

/**
 * The ConsoleCommandManager class is responsible for managing console commands.
 * It includes methods to register commands and their aliases, and to start the console thread.
 *
 * @since alpha.4
 */
@Slf4j
public class ConsoleCommandManager {

    @Getter(AccessLevel.PROTECTED)
    private HashMap<String, String> aliases = new HashMap<>();

    @Getter(AccessLevel.PROTECTED)
    private HashMap<String, ConsoleCommandExecutor> commands = new HashMap<>();

    /**
     * Constructs a new ConsoleCommandManager and registers all classes annotated with ConsoleCommand.
     *
     * @param mainClass The main class of the application.
     * @since alpha.4
     */
    public ConsoleCommandManager(Class<?> mainClass) {
        Reflections reflections = new Reflections(mainClass.getPackageName());

        reflections.getTypesAnnotatedWith(ConsoleCommandDefinition.class).forEach(clazz -> {

            ConsoleCommandDefinition annotation = clazz.getAnnotation(ConsoleCommandDefinition.class);

            String name = annotation.name();

            for (String alias : annotation.aliases()) {
                aliases.put(alias, name);
            }

            ConsoleCommandExecutor executor = (ConsoleCommandExecutor) JDABootObjectManager.getOrInitialiseObject(clazz);
            commands.put(name, executor);
            log.info("Registered console command {}", clazz.getName());
        });

        ConsoleThread consoleThread = new ConsoleThread(this);
        consoleThread.start();
    }

    /**
     * Executes the specified command with the provided arguments.
     * If the command is not found, it checks if the command is an alias for another command and executes that.
     * If the command is still not found, it logs a warning.
     *
     * @param command The command to execute.
     * @since alpha.4
     */
    protected void runCommand(String command) {

        String cmd = command.split(" ")[0];
        String[] args;
        if (command.trim().equals(cmd)) {
            args = new String[0];
        } else {
            args = command.replace(cmd, "").trim().split(" ");
        }

        if (commands.containsKey(cmd)) {
            new Thread(() -> {
                Thread.currentThread().setName("ConsoleThread-" + cmd);
                commands.get(cmd).onCommand(args);
            }).start();
        } else if (aliases.containsKey(cmd)) {
            new Thread(() -> {
                Thread.currentThread().setName("ConsoleThread-" + cmd);
                commands.get(aliases.get(cmd)).onCommand(args);
            }).start();
        } else {
            log.warn("Command not found!");
        }
    }

}
