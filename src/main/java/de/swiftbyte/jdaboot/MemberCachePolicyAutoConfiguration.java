package de.swiftbyte.jdaboot;

import net.dv8tion.jda.api.utils.MemberCachePolicy;

/**
 * The MemberCachePolicyAutoConfiguration enum is used to map the JDA's MemberCachePolicy options to enum constants.
 * This makes it easier to use and manage the MemberCachePolicy options in the application.
 */
public enum MemberCachePolicyAutoConfiguration {

    DEFAULT,
    ALL,
    ONLINE,
    VOICE,
    OWNER,
    PENDING,
    NONE;

    /**
     * This method is used to get the corresponding JDA's MemberCachePolicy for the enum constant.
     *
     * @return The corresponding JDA's MemberCachePolicy.
     */
    public MemberCachePolicy getJDAUtilsMemberCachePolicy() {
        return switch (this) {
            case ALL -> MemberCachePolicy.ALL;
            case ONLINE -> MemberCachePolicy.ONLINE;
            case VOICE -> MemberCachePolicy.VOICE;
            case OWNER -> MemberCachePolicy.OWNER;
            case PENDING -> MemberCachePolicy.PENDING;
            case NONE -> MemberCachePolicy.NONE;
            default -> MemberCachePolicy.DEFAULT;
        };
    }
}