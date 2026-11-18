package de.swiftbyte.jdaboot.context.fixtures.external;

import de.swiftbyte.jdaboot.annotation.context.Bean;
import de.swiftbyte.jdaboot.annotation.context.Component;
import de.swiftbyte.jdaboot.annotation.context.Configuration;

@Configuration
public class ExternalConfiguration {
    @Bean
    public String externalValue() {
        throw new AssertionError("Discovery invoked an external factory");
    }

    @Component
    public static class UnimportedNeighbour {
    }
}
