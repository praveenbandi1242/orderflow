package com.orderflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.config.AbstractIntegrationTest;
import com.orderflow.dto.CreateProductRequest;
import com.orderflow.dto.UpdateProductRequest;
import com.orderflow.entity.Product;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ProductIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setup() {

        product = Product.builder()
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .deleted(false)
                .build();

        product = productRepository.save(product);
    }

    @Test
    @DisplayName("Create Product")
    void createProduct_ShouldReturn201() throws Exception {

        CreateProductRequest request = new CreateProductRequest();
        request.setName("iPhone 16");
        request.setPrice(BigDecimal.valueOf(75000));
        request.setStock(20);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("iPhone 16"))
                .andExpect(jsonPath("$.data.stock").value(20));
    }

    @Test
    @DisplayName("Get Product By Id")
    void getProductById_ShouldReturn200() throws Exception {

        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(product.getId()))
                .andExpect(jsonPath("$.data.name").value("MacBook Air"));
    }

    @Test
    @DisplayName("Get All Products")
    void getProducts_ShouldReturn200() throws Exception {

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Update Product")
    void updateProduct_ShouldReturn200() throws Exception {

        Product product = productRepository.findByDeletedFalse().getFirst();
        Long id = product.getId();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("MacBook Pro");
        request.setPrice(BigDecimal.valueOf(120000));
        request.setStock(5);

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("MacBook Pro"))
                .andExpect(jsonPath("$.data.stock").value(5));
    }

    @Test
    @DisplayName("Delete Product")
    void deleteProduct_ShouldReturn200() throws Exception {

        Product product = productRepository.findByDeletedFalse().getFirst();
        Long id = product.getId();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create Product Validation")
    void createProduct_ShouldReturn400() throws Exception {

        CreateProductRequest request = new CreateProductRequest();
        request.setName("");
        request.setPrice(BigDecimal.valueOf(-100));
        request.setStock(0);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Product Not Found")
    void getProduct_ShouldReturn404() throws Exception {

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }
}