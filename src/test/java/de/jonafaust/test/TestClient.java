package de.jonafaust.test;

import de.jonafaust.jdaboot.JDABoot;
import de.jonafaust.jdaboot.annotation.EventHandler;
import net.dv8tion.jda.api.events.session.ReadyEvent;

public class TestClient {

    public static void main(String[] args) {
        JDABoot.run(TestClient.class);
    }

    @EventHandler
    public void onReady(ReadyEvent event) {
        System.out.println("Ready from LoginTest");
    }

}
