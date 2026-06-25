package com.orderflow.service;

import com.orderflow.dto.CreateProductRequest;
import com.orderflow.dto.ProductResponse;
import com.orderflow.dto.UpdateProductRequest;
import com.orderflow.dto.common.PageResponse;
import com.orderflow.entity.Product;
import com.orderflow.exception.InsufficientStockException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.mapper.ProductMapper;
import com.orderflow.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {

        // Arrange (Given)
        CreateProductRequest request = new CreateProductRequest();
        request.setName("MacBook Air");
        request.setPrice(BigDecimal.valueOf(99999));
        request.setStock(10);

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        // Mock behavior
        when(productMapper.toEntity(request))
                .thenReturn(product);

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        // Act (When)
        ProductResponse result = productService.createProduct(request);

        // Assert (Then)
        assertNotNull(result);
        assertEquals("MacBook Air", result.getName());

        verify(productMapper).toEntity(request);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class,
                        () -> productService.getProductById(1L));

        assertEquals("Product not found with id : 1",
                exception.getMessage());

        verify(productRepository).findById(1L);
        verifyNoInteractions(productMapper);
    }
    @Test
    void shouldGetAllProducts() {

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        when(productRepository.findByDeletedFalse())
                .thenReturn(List.of(product));

        when(productMapper.toResponse(product))
                .thenReturn(response);

        List<ProductResponse> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MacBook Air", result.getFirst().getName());

        verify(productRepository).findByDeletedFalse();
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldGetProductById() {

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .deleted(false)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("MacBook Air", result.getName());

        verify(productRepository).findById(1L);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldGetProductsWhenKeywordIsBlank() {

        Page<Product> page = new PageImpl<>(List.of());

        when(productRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(page);

        PageResponse<ProductResponse> result =
                productService.getProducts("", 0, 5, "id", "asc");

        assertNotNull(result);
        assertEquals(0, result.getContent().size());

        verify(productRepository)
                .findByDeletedFalse(any(Pageable.class));
    }

    @Test
    void shouldUpdateProduct() {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("MacBook Pro");
        request.setPrice(BigDecimal.valueOf(150000));
        request.setStock(20);

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .deleted(false)
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .price(BigDecimal.valueOf(150000))
                .stock(20)
                .deleted(false)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("MacBook Pro")
                .price(BigDecimal.valueOf(150000))
                .stock(20)
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(updatedProduct);

        when(productMapper.toResponse(updatedProduct))
                .thenReturn(response);

        ProductResponse result = productService.updateProduct(1L, request);

        assertNotNull(result);
        assertEquals("MacBook Pro", result.getName());
        assertEquals(20, result.getStock());

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toResponse(updatedProduct);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingProductNotFound() {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("MacBook Pro");
        request.setPrice(BigDecimal.valueOf(150000));
        request.setStock(20);

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, request));

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
        verifyNoInteractions(productMapper);
    }

    @Test
    void shouldSoftDeleteProduct() {

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .deleted(false)
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertTrue(product.isDeleted());

        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenProductIsDeleted() {

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .deleted(true)
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class,
                        () -> productService.getProductById(1L));

        assertEquals("Product not found with id : 1",
                exception.getMessage());

        verify(productRepository).findById(1L);
        verifyNoInteractions(productMapper);
    }

    @Test
    void shouldThrowExceptionWhenDeletingProductNotFound() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(1L));

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldReduceStock() {

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        when(productRepository.save(product))
                .thenReturn(product);

        productService.reduceStock(product, 3);

        assertEquals(7, product.getStock());

        verify(productRepository).save(product);
    }

    @Test
    void shouldReduceStockToZero() {

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(5)
                .build();

        when(productRepository.save(product))
                .thenReturn(product);

        productService.reduceStock(product, 5);

        assertEquals(0, product.getStock());

        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(3)
                .build();

        InsufficientStockException exception =
                assertThrows(InsufficientStockException.class,
                        () -> productService.reduceStock(product, 5));

        assertEquals("Insufficient stock",
                exception.getMessage());

        verify(productRepository, never()).save(any());
    }

//    without keyword

    @Test
    void shouldGetProductsWithoutKeyword() {

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(page);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        PageResponse<ProductResponse> result =
                productService.getProducts(null, 0, 5, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(productRepository)
                .findByDeletedFalse(any(Pageable.class));

        verify(productMapper).toResponse(product);
    }

//    with keyword

    @Test
    void shouldGetProductsWithKeyword() {

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(99999))
                .stock(10)
                .build();

        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findByNameContainingIgnoreCaseAndDeletedFalse(
                eq("Mac"),
                any(Pageable.class)))
                .thenReturn(page);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        PageResponse<ProductResponse> result =
                productService.getProducts("Mac", 0, 5, "id", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(productRepository)
                .findByNameContainingIgnoreCaseAndDeletedFalse(
                        eq("Mac"),
                        any(Pageable.class));

        verify(productMapper).toResponse(product);
    }
}