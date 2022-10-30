package de.jonafaust.test;

import de.jonafaust.jdaboot.annotation.Command;
import de.jonafaust.jdaboot.command.CommandContext;
import de.jonafaust.jdaboot.command.SimpleCommand;

@Command(name = "test", description = "Test command")
public class TestCommand implements SimpleCommand {

    @Override
    public void onCommand(CommandContext ctx) {

        /*MessageEmbed embed = new MessageEmbed(
                "https://hufeisen-games.de",
                "Test",
                "Test description",
                EmbedType.VIDEO,
                OffsetDateTime.now(),
                65280,
                new MessageEmbed.Thumbnail("https://cdn.discordapp.com/avatars/424271496245149698/6a205d13086c2bf5caf3b1c71f1c3ab3.png", null, 1000, 1000),
                null,
                new MessageEmbed.AuthorInfo("Hufeisen", "https://hufeisen-games.de/team/hufeisen", "https://cdn.discordapp.com/avatars/424271496245149698/6a205d13086c2bf5caf3b1c71f1c3ab3.png", null),
                new MessageEmbed.VideoInfo("https://file-examples.com/wp-content/uploads/2017/04/file_example_MP4_1280_10MG.mp4", 100, 100),
                new MessageEmbed.Footer("test Footer", "https://cdn.discordapp.com/avatars/424271496245149698/6a205d13086c2bf5caf3b1c71f1c3ab3.png", null),
                null,
                List.of()
        );*/


        ctx.replyEmbeds(EmbedClass.embed).queue();
    }
}
