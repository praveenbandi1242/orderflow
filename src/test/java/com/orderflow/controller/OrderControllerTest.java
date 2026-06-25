package com.orderflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderResponse;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should Create Order")
    void shouldCreateOrder() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .productName("MacBook Air")
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(200000))
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.productName").value("MacBook Air"))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    @DisplayName("Should Return Bad Request When Validation Fails")
    void shouldReturnBadRequestWhenValidationFails() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should Get Order By Id")
    void shouldGetOrderById() throws Exception {

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .productName("MacBook Air")
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(200000))
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getOrderById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.productName").value("MacBook Air"))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    @DisplayName("Should Return 404 When Order Not Found")
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {

        when(orderService.getOrderById(1L))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should Get All Orders")
    void shouldGetAllOrders() throws Exception {

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .productName("MacBook Air")
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(200000))
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getAllOrders())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Orders fetched successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].productName").value("MacBook Air"));
    }

    @Test
    @DisplayName("Should Cancel Order")
    void shouldCancelOrder() throws Exception {

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .productName("MacBook Air")
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(200000))
                .status("CANCELLED")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.cancelOrder(1L))
                .thenReturn(response);

        when(orderService.getOrderById(1L))
                .thenReturn(response);

        mockMvc.perform(put("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should Return 404 When Cancelling Non Existing Order")
    void shouldReturnNotFoundWhenCancellingOrder() throws Exception {

        when(orderService.cancelOrder(eq(1L)))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(put("/api/orders/1/cancel"))
                .andExpect(status().isNotFound());
    }

}