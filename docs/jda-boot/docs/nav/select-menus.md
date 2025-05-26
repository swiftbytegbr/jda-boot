# Select Menus

Select Menus in JDA-Boot allow users to make selections from a predefined list of options. They can be configured using
annotations and executed when a user interacts with them. This page explains how to configure and use select menus in
your bot.

## Select Menu Configuration

To create a select menu, a class must implement the appropriate executor interface (`StringSelectMenuExecutor` or
`EntitySelectMenuExecutor`) and be annotated with the corresponding annotation (`@StringSelectMenuDefinition` or
`@EntitySelectMenuDefinition`). Variables can be defined in the @SelectMenuDefinition for values whose value is not yet
clear at the start. These are set later when the correct JDA select menu is generated and attached to an event. It is
still possible to define default variables. If the same key is later given a value, the default value is overwritten. To
use a variable, ${VARIABLE_KEY} must be entered in the respective field.

### @StringSelectMenuDefinition

The `@StringSelectMenuDefinition` annotation is used to define a select menu with string-based options.

| Annotation Field | Description                                                              | Data Type            |
|------------------|--------------------------------------------------------------------------|----------------------|
| `options`        | The options available in the menu, configured with `@StringSelectOption` | StringSelectOption[] |
| `placeholder`    | The placeholder text displayed in the menu                               | String               |
| `minOptions`     | The minimum number of options a user must select                         | Integer              |
| `maxOptions`     | The maximum number of options a user can select                          | Integer              |

### @StringSelectOption

The `@StringSelectOption` annotation is used to define individual options for a string select menu.

| Annotation Field | Description                                        | Data Type |
|------------------|----------------------------------------------------|-----------|
| `label`          | The label displayed for the option                 | String    |
| `value`          | The value associated with the option               | String    |
| `description`    | A description for the option (optional)            | String    |
| `emoji`          | An emoji displayed alongside the option (optional) | String    |

### @EntitySelectMenuDefinition

The `@EntitySelectMenuDefinition` annotation is used to define a select menu for selecting Discord entities (e.g.,
users, roles, channels).

| Annotation Field | Description                                      | Data Type |
|------------------|--------------------------------------------------|-----------|
| `placeholder`    | The placeholder text displayed in the menu       | String    |
| `enableUser`     | Whether users can be selected                    | Boolean   |
| `enableRole`     | Whether roles can be selected                    | Boolean   |
| `enableChannel`  | Whether channels can be selected                 | Boolean   |
| `minOptions`     | The minimum number of options a user must select | Integer   |
| `maxOptions`     | The maximum number of options a user can select  | Integer   |

## Select Menu Execution

If the select menu class implements the appropriate executor interface, the corresponding method must be implemented:

- For string select menus: `onSelectMenuSubmit(StringSelectInteractionEvent event, HashMap<String, String> variables)`
- For entity select menus: `onSelectMenuSubmit(EntitySelectInteractionEvent event, HashMap<String, String> variables)`

The `event` parameter provides access to the interaction event, and the `variables` parameter is responsible for the
variable transfer. The variable transfer only works with randomly generated IDs and only if the button was generated
after the last restart. The variables specified during menu initialization can then be found in this HashMap.

!!! example
=== "Java"
```java
@StringSelectMenuDefinition(
options = {
@StringSelectOption(label = "Option 1", value = "Value1"),
@StringSelectOption(label = "Option 2", value = "Value2")
},
placeholder = "Choose an option",
maxOptions = 1
)
public class ExampleStringSelectMenu implements StringSelectMenuExecutor {

            @Override
            public void onSelectMenuSubmit(StringSelectInteractionEvent event, HashMap<String, String> variables) {
                String selectedValue = event.getSelectedOptions().get(0).getValue();
                event.reply("You selected: " + selectedValue).queue();
            }
        }
        ```
        
        ```java
        @EntitySelectMenuDefinition(
                placeholder = "Select a channel",
                enableChannel = true
        )
        public class ExampleEntitySelectMenu implements EntitySelectMenuExecutor {

            @Override
            public void onSelectMenuSubmit(EntitySelectInteractionEvent event, HashMap<String, String> variables) {
                String selectedChannel = event.getValues().get(0).getName();
                event.reply("You selected the channel: " + selectedChannel).queue();
            }
        }
        ```

## Using Select Menus

To use a select menu, it must be embedded in other parts of JDA-Boot, such as modals or buttons. This is done using the
`@StringSelectMenuByClass` or `@EntitySelectMenuByClass` annotations, which must annotate a field of type
`TemplateSelectMenu`. At runtime, JDA-Boot initializes the field with the corresponding `TemplateSelectMenu`.

An `AdvancedSelectMenu` can be created from the `TemplateSelectMenu` using the `advancedSelectMenu()` method. Variables
can be set using the `setVariable(key, value)` method, and dynamic options can be added to string select menus using the
`addDynamicOption(label, value)` method. Finally, the select menu can be built using the `build()` method.

!!! example
=== "Java"
```java
@SlashCommandDefinition(
name = "selectmenu",
description = "Demonstrates select menus",
type = SlashCommandDefinition.Type.SLASH
)
public class SelectMenuCommand extends SlashCommandExecutor {

            @StringSelectMenuByClass(ExampleStringSelectMenu.class)
            private TemplateSelectMenu stringMenu;
            
            @EntitySelectMenuByClass(ExampleEntitySelectMenu.class)
            private TemplateSelectMenu entityMenu;

            @Override
            public void onCommand() {
                event.reply("Choose an option:")
                        .addActionRow(stringMenu.advancedSelectMenu()
                                .addDynamicOption("Dynamic Option", "DynamicValue")
                                .build())
                        .addActionRow(entityMenu.advancedSelectMenu().build())
                        .queue();
            }
        }
        ```