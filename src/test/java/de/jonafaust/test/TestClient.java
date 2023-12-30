package de.jonafaust.test;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.annotation.AutoConfiguration;
import de.swiftbyte.jdaboot.annotation.SetValue;
import de.swiftbyte.jdaboot.configuration.YmlConfigProviderImpl;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

@AutoConfiguration(
        configProvider = YmlConfigProviderImpl.class
)
@Slf4j
public class TestClient {

    @SetValue("app.version")
    public static String version;

    public static void main(String[] args) {
        JDABoot.run(TestClient.class, MemberCachePolicy.DEFAULT, GatewayIntent.getIntents(GatewayIntent.DEFAULT).toArray(new GatewayIntent[]{}));
        JDABoot.getInstance().getJda().getGatewayIntents().forEach(System.out::println);
        System.out.println(("Version: " + version));
        JDABoot.getInstance().updateCommands("774993548579045386");
    }

}
