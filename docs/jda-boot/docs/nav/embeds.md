# Embeds

Embeds in JDA-Boot allow you to create rich, styled messages for Discord. They can be configured using annotations and support dynamic variables, making them highly customizable. This page explains how to configure and use embeds in your bot.

## Embed Configuration

Embeds are defined using the `@Embed` annotation. This annotation allows you to configure various aspects of the embed, such as its title, description, author, footer, fields, and more. You can also define default variables that can be replaced dynamically at runtime.

### @Embed

The `@Embed` annotation has several fields for customization. The `id` field is required, and it is recommended to set a `title` and `description` for the embed.

| Annotation Field | Description                                                         | Data Type         |
|------------------|---------------------------------------------------------------------|-------------------|
| `id`             | The unique identifier for the embed                                 | String            |
| `basedOn`        | The ID of another embed to inherit properties from                  | String            |
| `title`          | The title of the embed, supports variables                          | String            |
| `description`    | The description of the embed, supports variables                    | String            |
| `color`          | The color of the embed                                              | EmbedColor        |
| `author`         | The author of the embed, configured with `@EmbedAuthor`             | EmbedAuthor       |
| `footer`         | The footer of the embed, configured with `@EmbedFooter`             | EmbedFooter       |
| `fields`         | The fields of the embed, configured with `@EmbedField`              | EmbedField[]      |
| `defaultVars`    | Default variables for the embed, configured with `@DefaultVariable` | DefaultVariable[] |

### @EmbedAuthor

The `@EmbedAuthor` annotation is used to configure the author section of the embed.

| Annotation Field | Description                | Data Type |
|------------------|----------------------------|-----------|
| `name`           | The name of the author     | String    |
| `url`            | The URL of the author      | String    |
| `iconUrl`        | The icon URL of the author | String    |

### @EmbedFooter

The `@EmbedFooter` annotation is used to configure the footer section of the embed.

| Annotation Field | Description                | Data Type |
|------------------|----------------------------|-----------|
| `text`           | The text of the footer     | String    |
| `iconUrl`        | The icon URL of the footer | String    |

### @EmbedField

The `@EmbedField` annotation is used to define fields within the embed.

| Annotation Field | Description                  | Data Type |
|------------------|------------------------------|-----------|
| `title`          | The title of the field       | String    |
| `description`    | The description of the field | String    |
| `inline`         | Whether the field is inline  | Boolean   |

### @DefaultVariable

The `@DefaultVariable` annotation is used to define default variables for the embed.

| Annotation Field | Description                       | Data Type |
|------------------|-----------------------------------|-----------|
| `variable`       | The key of the variable           | String    |
| `value`          | The default value of the variable | String    |

## Using Embeds

To use an embed, you can create an `AdvancedEmbed` from a `TemplateEmbed` using the `advancedEmbed()` method. Variables can be set dynamically using the `setVariable(key, value)` method. You can also add dynamic fields using the `addDynamicField(title, description, inline)` method. Once configured, the embed can be built using the `build()` method and sent as part of a Discord message.

!!! example
    === "Java"
        ```java
        public class EmbedClass {

            @Embed(
                id = "exampleEmbed",
                title = "Example Title",
                description = "This is an example embed.",
                color = EmbedColor.BLUE,
                author = @EmbedAuthor(
                    name = "Author Name",
                    iconUrl = "https://example.com/icon.png"
                ),
                footer = @EmbedFooter(
                    text = "Footer Text"
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
                        inline = false
                    )
                }
            )
            public static TemplateEmbed exampleEmbed;
            
            public void sendEmbed(SlashCommandInteractionEvent event) {
                AdvancedEmbed advancedEmbed = exampleEmbed.advancedEmbed();
                advancedEmbed.addDynamicField("Dynamic Field", "Dynamic Description", false);
    
                event.replyEmbeds(advancedEmbed.build()).queue();
            }
        }
        ```

## Inheriting Embeds

Embeds can inherit properties from another embed using the `basedOn` field. This allows you to define a base embed with common properties and extend it in other embeds.

!!! example
    === "Java"
        ```java
        public class EmbedClass {

            @Embed(
                id = "baseEmbed",
                color = EmbedColor.GREEN
            )
            public static TemplateEmbed baseEmbed;
            
            @Embed(
                    basedOn = "baseEmbed",
                    id = "childEmbed",
                    title = "Child Embed Title",
                    description = "This embed inherits from the base embed."
            )
            public static TemplateEmbed childEmbed;
        }
        ```

## Dynamic Variables

Embeds support dynamic variables, which can be replaced at runtime. Variables are defined using the `${variable}` syntax in fields like `title`, `description`, and others. These variables can be set using the `setVariable(key, value)` method of `AdvancedEmbed`.

!!! example
    === "Java"
        ```java
        public class EmbedClass {

            @Embed(
                title = "Example Title",
                description = "${description}",
            )
            public static TemplateEmbed exampleEmbed;
    
            public void sendEmbed(SlashCommandInteractionEvent event) {
                AdvancedEmbed advancedEmbed = exampleEmbed.advancedEmbed();
                advancedEmbed.setVariable("description", "Example Description");
                event.replyEmbeds(advancedEmbed.build()).queue();
            }
        }
        ```