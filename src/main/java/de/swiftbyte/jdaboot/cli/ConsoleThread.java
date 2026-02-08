package de.swiftbyte.jdaboot.cli;

import lombok.CustomLog;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * The ConsoleThread class is responsible for reading console input and executing the corresponding commands.
 * It uses a BufferedReader to read console input and splits the input into command and arguments.
 * The command is then passed to the ConsoleCommandManager for execution.
 *
 * @since alpha.4
 */
@CustomLog
public class ConsoleThread extends Thread {

    private @NonNull ConsoleCommandManager consoleCommandManager;
    private @NonNull BufferedReader br;

    /**
     * Constructs a new ConsoleThread with the provided ConsoleCommandManager.
     *
     * @param consoleCommandManager The ConsoleCommandManager to use for executing commands.
     * @since alpha.4
     */
    public ConsoleThread(@NonNull ConsoleCommandManager consoleCommandManager) {

        this.setName("ConsoleThread");

        this.consoleCommandManager = consoleCommandManager;
        br = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * The main run method of the thread.
     * It continuously reads console input and passes the commands to the ConsoleCommandManager for execution.
     *
     * @since alpha.4
     */
    @Override
    public void run() {

        String line;
        try {
            while ((line = br.readLine()) != null) {
                consoleCommandManager.runCommand(line);
            }
        } catch (Exception e) {
            log.error("Error while reading console input!", e);
        }

    }
}
