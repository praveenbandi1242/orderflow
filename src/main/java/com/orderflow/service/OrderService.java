package com.orderflow.service;

import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderResponse;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.entity.Order;
import com.orderflow.entity.OrderStatus;
import com.orderflow.entity.Product;
import com.orderflow.mapper.OrderMapper;
import com.orderflow.repository.OrderRepository;
import com.orderflow.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request){

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product Not Found with id : " + request.getProductId()));

        log.info(
                "Creating Order | productId={} quantity={}",
                request.getProductId(),
                request.getQuantity()
        );

        log.info(
                "Current Stock={}",
                product.getStock()
        );

        productService.reduceStock(product, request.getQuantity());

        log.info(
                "Stock Updated={}",
                product.getStock()
        );

        BigDecimal total = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .product(product)
                .quantity(request.getQuantity())
                .totalPrice(total)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        log.info(
                "Order Created Successfully | id={} totalPrice={}",
                order.getId(),
                order.getTotalPrice()
        );

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id : " + id));

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id : " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return orderMapper.toResponse(order);
        }

        Product product = order.getProduct();

        product.setStock(product.getStock() + order.getQuantity());

        productRepository.save(product);

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

}