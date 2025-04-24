# Events

The event system in JDA-Boot allows you to handle Discord events in a structured and efficient way. By using annotations, you can define event handlers and integrate them into your bot. This page explains how to configure and use the event system in your project.

## Event Configuration

To handle an event, you need to create a method annotated with `@EventHandler`. This method will be executed whenever the specified event occurs. The method must have a single parameter, which is the event class you want to handle.

## Event Execution

When an event occurs, JDA-Boot automatically calls the corresponding method annotated with `@EventHandler`. The event object is passed as a parameter, allowing you to access all relevant information about the event.

!!! example
    === "Java"
        ```java
        public class MemberJoinEventHandler {

            @EventHandler
            public void onMemberJoin(GuildMemberJoinEvent event) {
                event.getGuild().getSystemChannel()
                        .sendMessage("Welcome " + event.getUser().getAsMention() + " to the server!")
                        .queue();
            }
        }
        ```

In this example, the `onMemberJoin` method is executed whenever a `GuildMemberJoinEvent` occurs. The method sends a welcome message to the system channel of the guild.

## Supported Events

JDA-Boot supports all events provided by JDA. You can handle any event by simply creating a method with the appropriate event class as its parameter and annotating it with `@EventHandler`.