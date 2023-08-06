package de.swiftbyte.jdaboot.command;

import de.swiftbyte.jdaboot.command.info.SlashCommandInfo;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommand {

    default void onEnable(SlashCommandData data) {
    }

    void onCommand(SlashCommandInfo ctx);

}
