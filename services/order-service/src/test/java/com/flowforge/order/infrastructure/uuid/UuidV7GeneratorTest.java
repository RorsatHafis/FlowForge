package com.flowforge.order.infrastructure.uuid;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UuidV7GeneratorTest {

    @Test
    void shouldGenerateUuidV7() {

        UUID uuid = UuidV7Generator.generate();

        assertNotNull(uuid);
        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant());

    }

}
