package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.cli.ConsoleCommandDefinition;
import de.swiftbyte.jdaboot.cli.ConsoleCommandExecutor;

@ConsoleCommandDefinition(name = "test", aliases = {"t" })
public class TestConsoleCommand implements ConsoleCommandExecutor {

    @Override
    public void onCommand(String[] args) {
        System.out.println("Test command executed!");
    }
}
