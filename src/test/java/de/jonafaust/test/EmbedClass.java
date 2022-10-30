package de.jonafaust.test;

import de.jonafaust.jdaboot.annotation.embed.Embed;
import de.jonafaust.jdaboot.annotation.embed.EmbedAuthor;
import de.jonafaust.jdaboot.annotation.embed.EmbedField;
import de.jonafaust.jdaboot.annotation.embed.EmbedFooter;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class EmbedClass {

    @Embed(
            title = "Test",
            description = "Test Description",
            author = @EmbedAuthor(
                    url = "https://hufeisen-games.de/team/hufeisen",
                    name = "Hufeisen",
                    iconUrl = "https://cdn.discordapp.com/avatars/424271496245149698/6a205d13086c2bf5caf3b1c71f1c3ab3.png"
            ),
            url = "https://hufeisen-games.de",
            color = 65280,
            imageUrl = "https://cdn.discordapp.com/avatars/424271496245149698/6a205d13086c2bf5caf3b1c71f1c3ab3.png",
            thumbnailUrl = "https://cdn.discordapp.com/avatars/424271496245149698/6a205d13086c2bf5caf3b1c71f1c3ab3.png",
            footer = @EmbedFooter(
                    iconUrl = "https://cdn.discordapp.com/avatars/424271496245149698/6a205d13086c2bf5caf3b1c71f1c3ab3.png",
                    text = "Test Footer"
            ),
            fields = {
                    @EmbedField(
                            title = "Field 1",
                            description = "Field 1 Description",
                            inline = true
                    ),
                    @EmbedField(
                            title = "Field 2",
                            description = "Field 2 Description",
                            inline = true
                    )
            }
    )
    public static MessageEmbed embed;

}
