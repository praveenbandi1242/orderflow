package com.orderflow.controller;

import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderResponse;
import com.orderflow.dto.common.ApiResponse;
import com.orderflow.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "Order APIs",
        description = "Operations related to Orders"
)
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Create Order
     */
    @Operation(
            summary = "Create Order",
            description = "Creates a new order"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse response = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order created successfully")
                        .timestamp(LocalDateTime.now())
                        .data(response)
                        .build());
    }

    /**
     * Get Order by Id
     */
    @Operation(
            summary = "Get Order",
            description = "Returns order by id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id) {

        OrderResponse response = orderService.getOrderById(id);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order fetched successfully")
                        .timestamp(LocalDateTime.now())
                        .data(response)
                        .build()
        );
    }

    /**
     * Get All Orders
     */
    @Operation(
            summary = "Get Orders",
            description = "Returns all orders"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {

        List<OrderResponse> response = orderService.getAllOrders();

        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .success(true)
                        .message("Orders fetched successfully")
                        .timestamp(LocalDateTime.now())
                        .data(response)
                        .build()
        );
    }

    /**
     * Cancel Order
     */
    @Operation(
            summary = "Cancel Order",
            description = "Cancels an existing order"
    )
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id) {

        orderService.cancelOrder(id);

        OrderResponse response = orderService.getOrderById(id);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .timestamp(LocalDateTime.now())
                        .data(response)
                        .build()
        );
    }
}