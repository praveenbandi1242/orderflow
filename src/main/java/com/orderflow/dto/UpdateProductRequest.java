package com.orderflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Positive(message = "Stock must be positive")
    private Integer stock;
}