package de.jonafaust.jdaboot.command;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface OptionCommand extends SimpleCommand{

    @Override
    SlashCommandData onEnable(SlashCommandData data);

}
