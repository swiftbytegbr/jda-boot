# Modals

Modals in JDA-Boot allow you to create interactive forms that users can fill out. They consist of a title and multiple
input fields, called rows. Modals can be configured using annotations and executed when submitted by a user. This page
explains how to configure and use modals in your bot.

## Modal Configuration

To create a modal, a class must implement the `ModalExecutor` interface and be annotated with the `@ModalDefinition`
annotation. Each modal consists of a title and one or more rows, which are defined using the `@ModalRow` annotation.
Variables can be defined in the @ModalDefinition for values whose value is not yet clear at the start. These are set
later when the correct JDA modal is generated and attached to an event. It is still possible to define default
variables. If the same key is later given a value, the default value is overwritten. To use a variable, ${VARIABLE_KEY}
must be entered in the respective field.

### @ModalDefinition

The `@ModalDefinition` annotation is used to define the overall configuration of the modal.

| Annotation Field | Description                                        | Data Type  |
|------------------|----------------------------------------------------|------------|
| `title`          | The title of the modal, supports variables         | String     |
| `rows`           | The rows of the modal, configured with `@ModalRow` | ModalRow[] |

### @ModalRow

The `@ModalRow` annotation is used to define individual input fields within the modal.

| Annotation Field | Description                                           | Data Type           |
|------------------|-------------------------------------------------------|---------------------|
| `id`             | The unique identifier for the row                     | String              |
| `label`          | The label displayed above the input field             | String              |
| `placeholder`    | The placeholder text displayed inside the input field | String              |
| `inputStyle`     | The style of the input field (SHORT or PARAGRAPH)     | ModalRow.InputStyle |
| `required`       | Whether the input field is required                   | Boolean             |
| `minLength`      | The minimum length of the input                       | Integer             |
| `maxLength`      | The maximum length of the input                       | Integer             |
| `defaultValue`   | The default value pre-filled in the input field       | String              |

## Modal Execution

If the modal class implements the `ModalExecutor` interface, the
`onModalSubmit(ModalInteractionEvent event, HashMap<String, String> variables)` method must be implemented. This method
is executed when the modal is submitted by a user. The `event` parameter provides access to the interaction event, and
the `variables` parameter contains any variables passed to the modal on creation.

!!! example
=== "Java"
```java
@ModalDefinition(
title = "Example Modal",
rows = {
@ModalRow(
id = "name",
label = "Your Name",
placeholder = "Enter your name",
inputStyle = ModalRow.InputStyle.SHORT,
required = true
),
@ModalRow(
id = "feedback",
label = "Your Feedback",
placeholder = "Enter your feedback",
inputStyle = ModalRow.InputStyle.PARAGRAPH,
minLength = 10,
maxLength = 200
)
}
)
public class ExampleModal implements ModalExecutor {

            @Override
            public void onModalSubmit(ModalInteractionEvent event, HashMap<String, String> variables) {
                String name = event.getValue("name").getAsString();
                String feedback = event.getValue("feedback").getAsString();

                event.reply("Thank you, " + name + ", for your feedback: " + feedback).setEphemeral(true).queue();
            }
        }
        ```

## Using Modals

To use a modal, it must be triggered by an interaction, such as a button or a command. The modal can be created and
displayed using the `event.replyModal()` method. Variables can be passed to the modal using the
`setVariable(key, value)` method of the `AdvancedModal`.

!!! example
=== "Java"
```java
public class CommandClass {

            @ModalByClass(ExampleModal.class)
            public TemplateModal templateModal;
            
            @SlashCommandDefinition(
                    name = "openmodal",
                    description = "Opens a modal",
                    type = SlashCommandDefinition.Type.SLASH
            )
            public class OpenModalCommand extends SlashCommandExecutor {
    
                @Override
                public void onCommand() {
                    AdvancedModal modal = templateModal.advancedModal();
                    event.replyModal(modal.build()).queue();
                }
            }
        }
        ```