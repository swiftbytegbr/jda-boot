# Buttons
Buttons have a configuration part and an execution part, similar to a command. However, there is extended logic to be able to use buttons easily and an option to use variables. Um einen Knopf zu erstellen muss eine Klasse die Klasse `ButtonExecutor` implementieren und von der `@ButtonDefinition` Annotation annotiert werden.

## Button Configuration
Variables can be defined in the `@ButtonDefinition` for values whose value is not yet clear at the start. These are set later when the correct JDA button is generated and attached to an event. It is still possible to define default variables. If the same key is later given a value, the default value is overwritten. To use a variable, `${VARIABLE_KEY}` must be entered in the respective field.

#### @ButtonDefinition
The fields `label` and `type` are required. If the `id` field is not set, JDA-Boot generates a new random ID each time it is booted. If a button already attached to a message should continue to work even after a restart of the bot, a fixed ID must be defined.

| Annotation Field   | Description                                                                               | Data Type                             |
| ------------------ | ----------------------------------------------------------------------------------------- | ------------------------------------- |
| `label`            | The label of the button                                                                   | String                                |
| `type`             | The type of the button                                                                    | String                                |
| `id`               | The id of the button                                                                      | String                                |
| `emoji`            | The emoji of the button                                                                   | String                                |
| `url`              | The url of the button                                                                     | String                                |
| `defaultVars`      | The default variables of the button                                                       | DefaultVariable[]                     |

## Button Execution
If the button class implements the `ButtonExecutor` interface, the method `onButtonClick(ButtonInteractionEvent event, HashMap<String, String> variables)` must be implemented. `event` is the triggered ButtonInteractionEvent and `variables` is responsible for the variable transfer. The variable transfer only works with randomly generated IDs and only if the button was generated after the last restart. The variables specified during button initialization can then be found in this HashMap.

!!! example
    === "Java"
        ```java
        @ButtonDefinition(
                label = "Test",
                id = "test_button",
                type = ButtonDefinition.Type.PRIMARY
        )
        public class TestButton implements ButtonExecutor {

            public void onButtonClick(ButtonInteractionEvent event, HashMap<String, String> variables) {
                event.reply("Button clicked!").queue();
            }
        }
        ```

## Using Buttons
To be able to use the previously created button, we need to embed it in other parts of JDA-Boot. For this, there are the annotations `@ButtonById` or `@ButtonByClass`, which must annotate a field with the type `TemplateButton`. On startup, JDA-Boot sets the respective field to the corresponding `TemplateButton`. To be able to use the button, an `AdvancedButton` must first be created from the `TemplateButton` using the `advancedButton()` or `advancedButton(discordLocale)` method. The second method is important if the bot is supposed to [implement different languages](translation-config.md#translation). Variables can then be set in the `AdvancedButton` using the `setVariable(key, value)` method. To finally pass the button to an event, a JDA `Button` must be created from the `AdvancedButton` using the `build()` method.

!!! example
    === "Java"
        ```java

        @ButtonByClass(TestButton.class)
        public TemplateButton button;

        @SlashCommandDefinition(
                name = "test",
                type = SlashCommandDefinition.Type.SLASH
        )
        public class TestCommand extends SlashCommandExecutor {

            @Override
            public void onCommand() {

                event.reply("Test").addActionRow(button.advancedButton().build()).queue();

            }
        }
        ```