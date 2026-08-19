package com.flowforge.order.outbox;

import java.util.UUID;

public interface OutboxService {

    void record(String aggregateType, UUID aggregateId, String eventType, Object payload);
    
}
