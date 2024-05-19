package de.swiftbyte.test;

import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import org.jetbrains.annotations.NotNull;

public class TestVoiceDispatchInterceptor implements VoiceDispatchInterceptor {

    public TestVoiceDispatchInterceptor() {
        System.out.println("VoiceDispatchInterceptor created.");
    }

    @Override
    public void onVoiceServerUpdate(@NotNull VoiceServerUpdate voiceServerUpdate) {

    }

    @Override
    public boolean onVoiceStateUpdate(@NotNull VoiceStateUpdate voiceStateUpdate) {
        return false;
    }
}
