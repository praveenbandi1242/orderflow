package com.orderflow.mapper;

import com.orderflow.dto.OrderResponse;
import com.orderflow.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "status", target = "status")
    OrderResponse toResponse(Order order);
}