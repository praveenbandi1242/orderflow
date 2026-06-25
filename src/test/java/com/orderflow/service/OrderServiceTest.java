package com.orderflow.service;

import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderResponse;
import com.orderflow.entity.Order;
import com.orderflow.entity.OrderStatus;
import com.orderflow.entity.Product;
import com.orderflow.exception.InsufficientStockException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.mapper.OrderMapper;
import com.orderflow.repository.OrderRepository;
import com.orderflow.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        Product product = Product.builder()
                .id(1L)
                .name("MacBook Air")
                .price(BigDecimal.valueOf(50000))
                .stock(10)
                .build();

        Order order = Order.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(100000))
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(100000))
                .status(String.valueOf(OrderStatus.CREATED))
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(response);

        OrderResponse result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals("CREATED", result.getStatus());

        verify(productRepository).findById(1L);
        verify(productService).reduceStock(product, 2);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toResponse(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(100L);
        request.setQuantity(2);

        when(productRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(request));

        verify(productRepository).findById(100L);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(1L);
        request.setQuantity(20);

        Product product = Product.builder()
                .id(1L)
                .price(BigDecimal.valueOf(50000))
                .stock(5)
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        doThrow(new InsufficientStockException("Insufficient stock"))
                .when(productService)
                .reduceStock(product, 20);

        assertThrows(InsufficientStockException.class,
                () -> orderService.createOrder(request));

        verify(productService).reduceStock(product, 20);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldReturnOrderById() {

        Product product = Product.builder()
                .id(1L)
                .build();

        Order order = Order.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(100000))
                .status(OrderStatus.CREATED)
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .status(String.valueOf(OrderStatus.CREATED))
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        OrderResponse result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(orderRepository).findById(1L);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {

        when(orderRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(100L));

        verify(orderRepository).findById(100L);
    }

    @Test
    void shouldReturnAllOrders() {

        Product product = Product.builder().id(1L).build();

        Order order = Order.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .status(OrderStatus.CREATED)
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .status(String.valueOf(OrderStatus.CREATED))
                .build();

        when(orderRepository.findAll())
                .thenReturn(List.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        List<OrderResponse> result = orderService.getAllOrders();

        assertEquals(1, result.size());

        verify(orderRepository).findAll();
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldReturnEmptyListWhenNoOrders() {

        when(orderRepository.findAll())
                .thenReturn(List.of());

        List<OrderResponse> result = orderService.getAllOrders();

        assertTrue(result.isEmpty());

        verify(orderRepository).findAll();
    }

    @Test
    void shouldCancelOrder() {

        Product product = Product.builder()
                .id(1L)
                .stock(5)
                .build();

        Order order = Order.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .status(OrderStatus.CREATED)
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .status("CANCELLED")
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        OrderResponse result = orderService.cancelOrder(1L);

        assertEquals("CANCELLED", result.getStatus());
        assertEquals(7, product.getStock());

        verify(productRepository).save(product);
        verify(orderRepository).save(order);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldReturnSameOrderWhenAlreadyCancelled() {

        Product product = Product.builder()
                .id(1L)
                .stock(5)
                .build();

        Order order = Order.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .status(OrderStatus.CANCELLED)
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .status("Cancelled")
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        OrderResponse result = orderService.cancelOrder(1L);

        assertEquals("Cancelled", result.getStatus());

        verify(productRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldThrowExceptionWhenCancelOrderNotFound() {

        when(orderRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.cancelOrder(10L));

        verify(orderRepository).findById(10L);
        verifyNoInteractions(productRepository);
    }
}