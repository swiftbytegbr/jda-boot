package de.swiftbyte.test;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.MemberCachePolicyConfiguration;
import de.swiftbyte.jdaboot.annotation.JDABootConfiguration;
import de.swiftbyte.jdaboot.annotation.SetValue;
import de.swiftbyte.jdaboot.configuration.YmlConfigProviderImpl;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@JDABootConfiguration(
        configProvider = YmlConfigProviderImpl.class,
        intents = {GatewayIntent.GUILD_MESSAGES},
        disabledCacheFlags = {CacheFlag.VOICE_STATE},
        memberCachePolicy = MemberCachePolicyConfiguration.DEFAULT
)
@Slf4j
public class TestClient {

    @SetValue("app.version")
    public static String version;

    public static void main(String[] args) {
        JDABoot.run(TestClient.class);
        JDABoot.getInstance().updateCommands("774993548579045386");
    }

}
