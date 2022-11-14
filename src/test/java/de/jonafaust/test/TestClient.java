package de.jonafaust.test;

import de.jonafaust.jdaboot.JDABoot;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class TestClient {

    public static final String NEPTUNVERSION = "1.0.0";

    public static void main(String[] args) {
        JDABoot.run(TestClient.class, GatewayIntent.values());
        JDABoot.getInstance().getJda().getGatewayIntents().forEach(System.out::println);
    }

}
