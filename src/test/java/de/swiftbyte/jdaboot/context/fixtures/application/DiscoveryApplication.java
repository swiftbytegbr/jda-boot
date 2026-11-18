package de.swiftbyte.jdaboot.context.fixtures.application;

import de.swiftbyte.jdaboot.annotation.context.Component;
import de.swiftbyte.jdaboot.annotation.context.Import;
import de.swiftbyte.jdaboot.context.fixtures.external.ExternalConfiguration;

@Import(ExternalConfiguration.class)
public class DiscoveryApplication {
    static {
        rejectInitialization();
    }

    private static void rejectInitialization() {
        throw new AssertionError("Discovery initialized an application class");
    }

    @Component
    public static class LocalService {
        static {
            rejectInitialization();
        }
    }
}
