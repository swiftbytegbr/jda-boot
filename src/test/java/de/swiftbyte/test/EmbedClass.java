package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.annotation.embed.EmbedAuthor;
import de.swiftbyte.jdaboot.annotation.embed.EmbedField;
import de.swiftbyte.jdaboot.annotation.embed.EmbedFooter;
import de.swiftbyte.jdaboot.embeds.EmbedColor;
import de.swiftbyte.jdaboot.embeds.TemplateEmbed;

public class EmbedClass {

    @Embed(
            title = "${test}Test",
            description = "Version: ?{app.version}",
            author = @EmbedAuthor(
                    name = "Hufeisen",
                    iconUrl = "https://cdn.discordapp.com/avatars/424271496245149698/6a205d13086c2bf5caf3b1c71f1c3ab3.png"
            ),
            color = EmbedColor.BLACK,
            footer = @EmbedFooter(
                    text = "Test"
            ),
            fields = {
                    @EmbedField(
                            title = "Field 1",
                            inline = true
                    ),
                    @EmbedField(
                            description = "Field 2 Description",
                            inline = true
                    )
            },
            defaultVars = {
                    @DefaultVariable(
                            variable = "test",
                            value = "`Mit Variablen`"
                    )
            }
    )
    public static TemplateEmbed embed;

}
