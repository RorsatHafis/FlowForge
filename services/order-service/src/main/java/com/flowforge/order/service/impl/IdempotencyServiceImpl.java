package com.flowforge.order.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowforge.order.entity.IdempotencyKey;
import com.flowforge.order.enums.IdempotencyStatus;
import com.flowforge.order.idempotency.IdempotencyResult;
import com.flowforge.order.idempotency.IdempotencyResultType;
import com.flowforge.order.repository.IdempotencyKeyRepository;
import com.flowforge.order.service.IdempotencyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final Duration IDEMPOTENCY_TTL =
            Duration.ofHours(24);

    private final IdempotencyKeyRepository repository;

    @Override
    @Transactional
    public IdempotencyResult checkAndStart (
            UUID customerId,
            String key,
            String requestHash
    ) {

        Instant now = Instant.now();

        var existing = repository.findByCustomerIdAndKey(
                customerId,
                key
        );

        if (existing.isPresent()) {

            IdempotencyKey idempotencyKey = existing.get();

            if (!idempotencyKey.getRequestHash().equals(requestHash)) {

                return new IdempotencyResult(
                        IdempotencyResultType.REQUEST_MISMATCH,
                        null,
                        null
                );

            }

            if (idempotencyKey.getExpiresAt().isBefore(now)) {

                return restart(
                        idempotencyKey,
                        now
                );

            }

            return switch (idempotencyKey.getStatus()) {

                case IN_PROGRESS -> new IdempotencyResult(
                        IdempotencyResultType.IN_PROGRESS,
                        null,
                        null
                );

                case COMPLETED -> new IdempotencyResult(
                        IdempotencyResultType.COMPLETED,
                        idempotencyKey.getResponseStatus(),
                        idempotencyKey.getResponseBody()
                );

                case FAILED -> restart(
                        idempotencyKey,
                        now
                );

            };

        }

        IdempotencyKey newKey = new IdempotencyKey();

        newKey.setCustomerId(customerId);
        newKey.setKey(key);
        newKey.setRequestHash(requestHash);
        newKey.setStatus(IdempotencyStatus.IN_PROGRESS);
        newKey.setExpiresAt(now.plus(IDEMPOTENCY_TTL));

         try {
            repository.saveAndFlush(newKey);

            return new IdempotencyResult(
                    IdempotencyResultType.NEW,
                    null,
                    null
            );

        } catch (DataIntegrityViolationException ex) {

            /*
             * Another concurrent request created the same
             * (customer_id, key) between our SELECT and INSERT.
             *
             * The database constraint correctly prevented
             * a duplicate idempotency record.
             */
            return handleConcurrentRequest(
                    customerId,
                    key,
                    requestHash
            );

        }

    }

    private IdempotencyResult restart (
            IdempotencyKey idempotencyKey,
            Instant now
    ) {

        idempotencyKey.setStatus(IdempotencyStatus.IN_PROGRESS);
        idempotencyKey.setResponseStatus(null);
        idempotencyKey.setResponseBody(null);
        idempotencyKey.setExpiresAt(
                now.plus(IDEMPOTENCY_TTL)
        );

        repository.save(idempotencyKey);

        return new IdempotencyResult(
                IdempotencyResultType.NEW,
                null,
                null
        );

    }


    private IdempotencyResult handleConcurrentRequest (
            UUID customerId,
            String key,
            String requestHash
    ) {

        IdempotencyKey existing = repository
                .findByCustomerIdAndKey(customerId, key)
                .orElseThrow();
                
        if (!existing.getRequestHash().equals(requestHash)) {

            return new IdempotencyResult(
                    IdempotencyResultType.REQUEST_MISMATCH,
                    null,
                    null
            );

        }

        if (existing.getExpiresAt().isBefore(Instant.now())) {

            return restart(
                    existing,
                    Instant.now()
            );

        }

        return switch (existing.getStatus()) {

            case IN_PROGRESS -> new IdempotencyResult(
                    IdempotencyResultType.IN_PROGRESS,
                    null,
                    null
            );

            case COMPLETED -> new IdempotencyResult(
                    IdempotencyResultType.COMPLETED,
                    existing.getResponseStatus(),
                    existing.getResponseBody()
            );

            case FAILED -> restart(
                    existing,
                    Instant.now()
            );
        };

    }

    @Override
    @Transactional
    public void complete (
            UUID customerId,
            String key,
            int responseStatus,
            String responseBody
    ) {

        IdempotencyKey idempotencyKey = repository
                .findByCustomerIdAndKey(customerId, key)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Idempotency key not found"
                        )
                );

        idempotencyKey.setStatus(IdempotencyStatus.COMPLETED);
        idempotencyKey.setResponseStatus(responseStatus);
        idempotencyKey.setResponseBody(responseBody);

        repository.save(idempotencyKey);

    }

    @Override
    @Transactional
    public void fail (
            UUID customerId,
            String key
    ) {

        IdempotencyKey idempotencyKey = repository
                .findByCustomerIdAndKey(customerId, key)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Idempotency key not found"
                        )
                );

        idempotencyKey.setStatus(IdempotencyStatus.FAILED);

        repository.save(idempotencyKey);

    }

}

