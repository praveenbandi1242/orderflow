package com.orderflow.service;

import com.orderflow.entity.Product;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    public void validateStock(Product product, Integer quantity) {

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient Stock");
        }
    }

    public void reduceStock(Product product, Integer quantity) {

        product.setStock(product.getStock() - quantity);
    }
}