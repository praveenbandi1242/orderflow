package com.orderflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.dto.CreateProductRequest;
import com.orderflow.dto.ProductResponse;
import com.orderflow.dto.UpdateProductRequest;
import com.orderflow.dto.common.PageResponse;
import com.orderflow.exception.GlobalExceptionHandler;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse getProduct() {

        return ProductResponse.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();
    }

    @Test
    @DisplayName("Create Product - Success")
    void createProduct_ShouldReturn201() throws Exception {

        CreateProductRequest request = new CreateProductRequest();
        request.setName("MacBook Air");
        request.setPrice(BigDecimal.valueOf(99999));
        request.setStock(10);

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(getProduct());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.name").value("MacBook Air"));
    }

    @Test
    @DisplayName("Create Product - Validation Failure")
    void createProduct_ShouldReturn400_WhenValidationFails() throws Exception {

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
    @DisplayName("Get Product By Id")
    void getProductById_ShouldReturn200() throws Exception {

        when(productService.getProductById(1L))
                .thenReturn(getProduct());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("MacBook Air"));
    }

    @Test
    @DisplayName("Get Product By Id - Not Found")
    void getProductById_ShouldReturn404() throws Exception {

        when(productService.getProductById(100L))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/100"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get Products")
    void getProducts_ShouldReturn200() throws Exception {

        PageResponse<ProductResponse> page = PageResponse.<ProductResponse>builder()
                .content(List.of(getProduct()))
                .page(0)
                .size(5)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(productService.getProducts(any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("MacBook Air"));
    }

    @Test
    @DisplayName("Update Product")
    void updateProduct_ShouldReturn200() throws Exception {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("MacBook Air M2");
        request.setPrice(BigDecimal.valueOf(109999));
        request.setStock(20);

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("MacBook Air M2")
                .price(BigDecimal.valueOf(109999))
                .stock(20)
                .build();

        when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("MacBook Air M2"));
    }

    @Test
    @DisplayName("Update Product - Not Found")
    void updateProduct_ShouldReturn404() throws Exception {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("MacBook");
        request.setPrice(BigDecimal.valueOf(50000));
        request.setStock(5);

        when(productService.updateProduct(eq(100L), any(UpdateProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(put("/api/products/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete Product")
    void deleteProduct_ShouldReturn200() throws Exception {

        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Delete Product - Not Found")
    void deleteProduct_ShouldReturn404() throws Exception {

        doThrow(new ResourceNotFoundException("Product not found"))
                .when(productService)
                .deleteProduct(100L);

        mockMvc.perform(delete("/api/products/100"))
                .andExpect(status().isNotFound());
    }

}