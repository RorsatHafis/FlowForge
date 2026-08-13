package com.flowforge.order.infrastructure.uuid;

import java.security.SecureRandom;
import java.util.UUID;

public final class UuidV7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final long TIMESTAMP_MASK = 0xFFFFFFFFFFFFL;
    private static final long RANDOM_A_MASK = 0xFFFL;
    private static final long RANDOM_B_MASK = 0x3FFFFFFFFFFFFFFFL;

    private UuidV7Generator () {}

    public static UUID generate () {

        long timestamp = System.currentTimeMillis();

        long randomA = RANDOM.nextLong() & RANDOM_A_MASK;
        long randomB = RANDOM.nextLong() & RANDOM_B_MASK;

        long mostSignificantBits =
                ((timestamp & TIMESTAMP_MASK) << 16)
                        | (0x7L << 12)
                        | randomA;

        long leastSignificantBits =
                (0x2L << 62)
                        | randomB;

        return new UUID(
                mostSignificantBits,
                leastSignificantBits
        );
        
    }
    
}
