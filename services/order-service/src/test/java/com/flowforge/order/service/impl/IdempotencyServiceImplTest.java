package com.flowforge.order.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flowforge.order.entity.IdempotencyKey;
import com.flowforge.order.enums.IdempotencyStatus;
import com.flowforge.order.idempotency.IdempotencyResult;
import com.flowforge.order.idempotency.IdempotencyResultType;
import com.flowforge.order.repository.IdempotencyKeyRepository;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceImplTest {

    @Mock
    private IdempotencyKeyRepository repository;

    @InjectMocks
    private IdempotencyServiceImpl service;

    private UUID customerId;
    private String key;
    private String requestHash;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        key = "test-key";
        requestHash = "hash-123";
    }

    @Test
    void shouldReturnNewWhenKeyDoesNotExist() {

        when(repository.findByCustomerIdAndKey(customerId, key))
                .thenReturn(java.util.Optional.empty());

        IdempotencyResult result = service.checkAndStart(
                customerId,
                key,
                requestHash
        );

        assertThat(result.getType())
                .isEqualTo(IdempotencyResultType.NEW);

        assertThat(result.getResponseStatus())
                .isNull();

        assertThat(result.getResponseBody())
                .isNull();

        verify(repository).saveAndFlush(any(IdempotencyKey.class));
    }

    @Test
    void shouldReturnInProgressWhenKeyIsAlreadyInProgress() {

        IdempotencyKey existing = createKey(
                IdempotencyStatus.IN_PROGRESS,
                requestHash
        );

        when(repository.findByCustomerIdAndKey(customerId, key))
                .thenReturn(java.util.Optional.of(existing));

        IdempotencyResult result = service.checkAndStart(
                customerId,
                key,
                requestHash
        );

        assertThat(result.getType())
                .isEqualTo(IdempotencyResultType.IN_PROGRESS);

        assertThat(result.getResponseStatus())
                .isNull();

        assertThat(result.getResponseBody())
                .isNull();

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void shouldReturnCompletedWithStoredResponse() {

        IdempotencyKey existing = createKey(
                IdempotencyStatus.COMPLETED,
                requestHash
        );

        existing.setResponseStatus(201);
        existing.setResponseBody(
                "{\"id\":\"order-123\"}"
        );

        when(repository.findByCustomerIdAndKey(customerId, key))
                .thenReturn(java.util.Optional.of(existing));

        IdempotencyResult result = service.checkAndStart(
                customerId,
                key,
                requestHash
        );

        assertThat(result.getType())
                .isEqualTo(IdempotencyResultType.COMPLETED);

        assertThat(result.getResponseStatus())
                .isEqualTo(201);

        assertThat(result.getResponseBody())
                .isEqualTo("{\"id\":\"order-123\"}");

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void shouldReturnRequestMismatchWhenHashIsDifferent() {

        IdempotencyKey existing = createKey(
                IdempotencyStatus.IN_PROGRESS,
                "original-hash"
        );

        when(repository.findByCustomerIdAndKey(customerId, key))
                .thenReturn(java.util.Optional.of(existing));

        IdempotencyResult result = service.checkAndStart(
                customerId,
                key,
                "different-hash"
        );

        assertThat(result.getType())
                .isEqualTo(IdempotencyResultType.REQUEST_MISMATCH);

        assertThat(result.getResponseStatus())
                .isNull();

        assertThat(result.getResponseBody())
                .isNull();

        verify(repository, never()).save(any());
        verify(repository, never()).saveAndFlush(any());
    }

    private IdempotencyKey createKey(
            IdempotencyStatus status,
            String hash
    ) {

        IdempotencyKey idempotencyKey = new IdempotencyKey();

        idempotencyKey.setCustomerId(customerId);
        idempotencyKey.setKey(key);
        idempotencyKey.setRequestHash(hash);
        idempotencyKey.setStatus(status);
        idempotencyKey.setExpiresAt(
                Instant.now().plusSeconds(3600)
        );

        return idempotencyKey;
    }
    
}