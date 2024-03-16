package de.swiftbyte.jdaboot.embeds;

import lombok.Getter;

import java.awt.*;

/**
 * The EmbedColor enum defines a set of predefined colors that can be used in embeds.
 * Each enum constant represents a color and is associated with a java.awt.Color instance.
 *
 * @since alpha.4
 */
@Getter
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
    YELLOW(Color.yellow),
    NOT_DEFINED(null);

    /**
     * The java.awt.Color instance associated with the enum constant.
     */
    private final Color color;

    /**
     * Constructor for EmbedColor. Initializes the enum constant with the specified java.awt.Color instance.
     *
     * @param color The java.awt.Color instance to associate with the enum constant.
     * @since alpha.4
     */
    EmbedColor(Color color) {

        this.color = color;

    }

}
