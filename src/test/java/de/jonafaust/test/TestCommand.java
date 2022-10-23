package de.jonafaust.test;

import de.jonafaust.jdaboot.annotation.Command;
import de.jonafaust.jdaboot.command.CommandContext;
import de.jonafaust.jdaboot.command.SimpleCommand;

@Command(name = "test", description = "Test command")
public class TestCommand implements SimpleCommand {

    @Override
    public void onCommand(CommandContext ctx) {
        ctx.reply("Hello World!");
    }

}
