package de.jonafaust.jdaboot.embeds;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.OffsetDateTime;
import java.util.List;

public class BootEmbed extends MessageEmbed {

    protected BootEmbed(String url, String title, String description, EmbedType type, OffsetDateTime timestamp, int color, Thumbnail thumbnail, Provider siteProvider, AuthorInfo author, VideoInfo videoInfo, Footer footer, ImageInfo image, List<Field> fields) {
        super(url, title, description, type, timestamp, color, thumbnail, siteProvider, author, videoInfo, footer, image, fields);
    }
}
