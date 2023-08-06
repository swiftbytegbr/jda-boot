package de.jonafaust.test;

import de.swiftbyte.jdaboot.JDABoot;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class TestClient {

    public static void main(String[] args) {
        JDABoot.run(TestClient.class, MemberCachePolicy.ALL, GatewayIntent.values());
        JDABoot.getInstance().getJda().getGatewayIntents().forEach(System.out::println);
    }

}
