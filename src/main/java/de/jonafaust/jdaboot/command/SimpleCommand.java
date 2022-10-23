package de.jonafaust.jdaboot.command;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SimpleCommand {

    default SlashCommandData onEnable(SlashCommandData data) {
        return data;
    }

    void onCommand(CommandContext ctx);

}
