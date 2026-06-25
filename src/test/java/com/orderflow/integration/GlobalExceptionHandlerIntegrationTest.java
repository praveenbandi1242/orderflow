package com.orderflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.CreateProductRequest;
import com.orderflow.entity.Product;
import com.orderflow.repository.OrderRepository;
import com.orderflow.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {

//        orderRepository.deleteAll();
//        productRepository.deleteAll();

        Product product = Product.builder()
                .name("MacBook Air")
                .price(BigDecimal.valueOf(100000))
                .stock(2)
                .deleted(false)
                .build();

        productRepository.save(product);
    }

    @Test
    @DisplayName("Resource Not Found - Product")
    void shouldReturn404ForProductNotFound() throws Exception {

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id : 999"));
    }

    @Test
    @DisplayName("Resource Not Found - Order")
    void shouldReturn404ForOrderNotFound() throws Exception {

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Order not found with id : 999"));
    }

    @Test
    @DisplayName("Validation Exception - Product")
    void shouldReturn400ForInvalidProduct() throws Exception {

        CreateProductRequest request = new CreateProductRequest();

        request.setName("");
        request.setPrice(BigDecimal.valueOf(-10));
        request.setStock(0);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stock").exists());
    }

    @Test
    @DisplayName("Validation Exception - Order")
    void shouldReturn400ForInvalidOrder() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest();

        request.setProductId(null);
        request.setQuantity(0);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.errors.productId").exists())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    @DisplayName("Insufficient Stock")
    void shouldReturn400WhenStockIsInsufficient() throws Exception {

        Long productId = productRepository.findAll().getFirst().getId();

        CreateOrderRequest request = new CreateOrderRequest();

        request.setProductId(productId);
        request.setQuantity(10);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient stock"));
    }
}