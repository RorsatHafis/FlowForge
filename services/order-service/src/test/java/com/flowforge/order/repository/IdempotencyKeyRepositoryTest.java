package com.flowforge.order.repository;

import com.flowforge.order.entity.IdempotencyKey;
import com.flowforge.order.enums.IdempotencyStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IdempotencyKeyRepositoryTest {

    @Autowired
    private IdempotencyKeyRepository repository;

    @Test
    void shouldFindIdempotencyKeyByCustomerIdAndKey() {
        UUID customerId = UUID.randomUUID();
        String key = "test-key-123";

        IdempotencyKey idempotencyKey = new IdempotencyKey();

        idempotencyKey.setCustomerId(customerId);
        idempotencyKey.setKey(key);
        idempotencyKey.setRequestHash("abc123");
        idempotencyKey.setStatus(IdempotencyStatus.IN_PROGRESS);
        idempotencyKey.setExpiresAt(
                Instant.now().plus(10, ChronoUnit.MINUTES)
        );

        repository.save(idempotencyKey);

        var result = repository.findByCustomerIdAndKey(
                customerId,
                key
        );

        assertThat(result).isPresent();
        assertThat(result.get().getCustomerId()).isEqualTo(customerId);
        assertThat(result.get().getKey()).isEqualTo(key);
        assertThat(result.get().getStatus())
                .isEqualTo(IdempotencyStatus.IN_PROGRESS);

    }
}