package de.jonafaust.jdaboot.embeds;

import lombok.Getter;

import java.awt.*;

public enum EmbedColor {

    BLACK(Color.black),
    BLUE(Color.blue),
    CYAN(Color.cyan),
    DARK_GRAY(Color.darkGray),
    GRAY(Color.gray),
    GREEN(Color.green),
    LIGHT_GRAY(Color.lightGray),
    MAGENTA(Color.magenta),
    ORANGE(Color.orange),
    PINK(Color.pink),
    RED(Color.red),
    WHITE(Color.white),
    YELLOW(Color.yellow);

    @Getter
    private final Color color;

    EmbedColor(Color color) {

        this.color = color;

    }

}
