package com.flowforge.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flowforge.order.dto.request.CreateOrderRequest;
import com.flowforge.order.dto.request.OrderItemRequest;
import com.flowforge.order.dto.response.ApiResponse;
import com.flowforge.order.dto.response.OrderResponse;
import com.flowforge.order.entity.Order;
import com.flowforge.order.entity.OrderItem;
import com.flowforge.order.exception.IdempotencyConflictException;
import com.flowforge.order.idempotency.IdempotencyResult;
import com.flowforge.order.mapper.OrderMapper;
import com.flowforge.order.repository.OrderItemRepository;
import com.flowforge.order.service.CreateOrderCommand;
import com.flowforge.order.service.CreateOrderItemCommand;
import com.flowforge.order.service.IdempotencyService;
import com.flowforge.order.service.OrderService;
import com.flowforge.order.util.HashUtil;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final OrderService orderService;
    private final IdempotencyService idempotencyService;
    private final OrderItemRepository orderItemRepository;
    private final JsonMapper jsonMapper;

    @PostMapping
    public ResponseEntity<?> createOrder (
        @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
        @Valid @RequestBody CreateOrderRequest request
    ) {

        String requestHash = hash(request);

        IdempotencyResult result = idempotencyService.checkAndStart(
                request.customerId(),
                idempotencyKey,
                requestHash
        );

        return switch (result.getType()) {

            case COMPLETED -> replay(result);

            case IN_PROGRESS -> throw new IdempotencyConflictException(
                    "A request with this idempotency key is still being processed."
            );

            case REQUEST_MISMATCH -> throw new IdempotencyConflictException(
                    "This idempotency key was already used with a different request body."
            );

            case NEW -> process(request, idempotencyKey);


        };

    }

    private ResponseEntity<?> process(CreateOrderRequest request, String idempotencyKey) {

        try {

            CreateOrderCommand command = toCommand(request);

            Order order = orderService.createOrder(command);

            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

            OrderResponse orderResponse = OrderMapper.toResponse(order, items);

            ApiResponse<OrderResponse> body = ApiResponse.success(orderResponse);

            idempotencyService.complete(
                    request.customerId(),
                    idempotencyKey,
                    HttpStatus.CREATED.value(),
                    writeJson(body)
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(body);

        } catch (RuntimeException ex) {

            idempotencyService.fail(request.customerId(), idempotencyKey);
            throw ex;

        }

    }

    private ResponseEntity<?> replay (IdempotencyResult result) {

        return ResponseEntity
                .status(result.getResponseStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(result.getResponseBody());

    }

    private CreateOrderCommand toCommand (CreateOrderRequest request) {

        List<CreateOrderItemCommand> items = request.items().stream()
                .map(this::toItemCommand)
                .toList();

        return new CreateOrderCommand(request.customerId(), items);

    }

    private CreateOrderItemCommand toItemCommand (OrderItemRequest item) {

        return new CreateOrderItemCommand(item.itemId(), item.quantity());

    }

    private String hash (CreateOrderRequest request) {

        return HashUtil.sha256(writeJson(request));

    }

    private String writeJson(Object value) {

        return jsonMapper.writeValueAsString(value);

    }

}
