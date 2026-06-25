package com.orderflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.config.AbstractIntegrationTest;
import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.entity.Order;
import com.orderflow.entity.OrderStatus;
import com.orderflow.entity.Product;
import com.orderflow.repository.OrderRepository;
import com.orderflow.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class OrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Product savedProduct;
    private Order savedOrder;

    @BeforeEach
    void setup() {

//        orderRepository.deleteAll();
//        productRepository.deleteAll();

        savedProduct = productRepository.save(
                Product.builder()
                        .name("MacBook Air")
                        .price(BigDecimal.valueOf(99999))
                        .stock(10)
                        .deleted(false)
                        .build()
        );

        savedOrder = orderRepository.save(
                Order.builder()
                        .product(savedProduct)
                        .quantity(2)
                        .totalPrice(BigDecimal.valueOf(199998))
                        .status(OrderStatus.CREATED)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    @DisplayName("Create Order")
    void createOrder_ShouldReturn201() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(savedProduct.getId());
        request.setQuantity(2);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("MacBook Air"))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    @DisplayName("Create Order Validation")
    void createOrder_ShouldReturn400() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(null);
        request.setQuantity(0);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create Order Product Not Found")
    void createOrder_ProductNotFound_ShouldReturn404() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(999L);
        request.setQuantity(2);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get Order By Id")
    void getOrderById_ShouldReturn200() throws Exception {

        mockMvc.perform(get("/api/orders/{id}", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(savedOrder.getId()))
                .andExpect(jsonPath("$.data.productName").value("MacBook Air"));
    }

    @Test
    @DisplayName("Order Not Found")
    void getOrder_ShouldReturn404() throws Exception {

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get All Orders")
    void getOrders_ShouldReturn200() throws Exception {

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Cancel Order")
    void cancelOrder_ShouldReturn200() throws Exception {

        mockMvc.perform(put("/api/orders/{id}/cancel", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Cancel Order Not Found")
    void cancelOrder_ShouldReturn404() throws Exception {

        mockMvc.perform(put("/api/orders/999/cancel"))
                .andExpect(status().isNotFound());
    }

}