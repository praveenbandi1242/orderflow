package com.orderflow.mapper;

import com.orderflow.dto.CreateProductRequest;
import com.orderflow.dto.ProductResponse;
import com.orderflow.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(CreateProductRequest request);

    ProductResponse toResponse(Product product);
}