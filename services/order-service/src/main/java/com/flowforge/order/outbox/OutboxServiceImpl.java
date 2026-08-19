package com.flowforge.order.outbox;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.flowforge.order.repository.OutboxRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;

    @Override
    public void record(String aggregateType, UUID aggregateId, String eventType, Object payload) {

        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(jsonMapper.writeValueAsString(payload))
                .build();
 
        outboxRepository.save(event);

    }
    
}
