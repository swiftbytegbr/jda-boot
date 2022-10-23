package de.jonafaust.test;

import de.jonafaust.jdaboot.JDABoot;
import de.jonafaust.jdaboot.annotation.Command;
import de.jonafaust.jdaboot.command.CommandContext;
import de.jonafaust.jdaboot.command.SimpleCommand;

@Command(name = "test", description = "Test command")
public class TestClient implements SimpleCommand {

    public static void main(String[] args) {
        JDABoot.run(TestClient.class);
    }

    @Override
    public void onCommand(CommandContext ctx) {
        ctx.reply("Hello, world").queue();
    }

}
