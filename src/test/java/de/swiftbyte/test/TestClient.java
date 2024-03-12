package de.swiftbyte.test;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.MemberCachePolicyConfiguration;
import de.swiftbyte.jdaboot.annotation.JDABootConfiguration;
import de.swiftbyte.jdaboot.annotation.Scheduler;
import de.swiftbyte.jdaboot.annotation.SetValue;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@JDABootConfiguration(
        intents = {GatewayIntent.GUILD_MESSAGES},
        disabledCacheFlags = {CacheFlag.VOICE_STATE},
        memberCachePolicy = MemberCachePolicyConfiguration.DEFAULT
)
@Slf4j
public class TestClient {

    @SetValue("app.version")
    private String version;

    public static void main(String[] args) {
        JDABoot.run(TestClient.class);
        JDABoot.getInstance().updateCommands("774993548579045386");
    }

    public static int testValue = 0;

    @Scheduler(interval = 1000 * 10, initialDelay = 5000)
    private boolean testScheduler() {

        testValue++;

        System.out.println("Scheduler test run " + testValue + " times.");

        return testValue < 10;
    }
}
