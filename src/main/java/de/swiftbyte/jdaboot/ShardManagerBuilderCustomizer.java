package de.swiftbyte.jdaboot;

import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.jspecify.annotations.NonNull;

/**
 * The ShardManagerBuilderCustomizer interface allows for customizing the DefaultShardManagerBuilder
 * before it is used to build the ShardManager. This can be useful for setting additional options
 * or configuring the builder in a specific way.
 *
 * @since 1.0.0-beta.2
 */
@FunctionalInterface
public interface ShardManagerBuilderCustomizer {

    /**
     * Customizes the DefaultShardManagerBuilder before it is used to build the ShardManager.
     *
     * @param builder The DefaultShardManagerBuilder to customize.
     * @since 1.0.0-beta.2
     */
    void customize(@NonNull DefaultShardManagerBuilder builder);
}
