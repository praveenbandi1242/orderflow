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
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    public ProductResponse createProduct(CreateProductRequest request) {

        log.info(
                "Creating Product | name={} price={} stock={}",
                request.getName(),
                request.getPrice(),
                request.getStock()
        );

        Product product = productMapper.toEntity(request);

        Product saved = productRepository.save(product);

        log.info(
                "Product Created Successfully | id={}",
                product.getId()
        );

        return productMapper.toResponse(saved);
    }

    public List<ProductResponse> getAllProducts() {

        return productRepository.findByDeletedFalse()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(String keyword,int page, int size,
                                                     String sortBy,
                                                     String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage;

        if (keyword == null || keyword.isBlank()) {
            productPage = productRepository.findByDeletedFalse(pageable);
        } else {
            productPage = productRepository
                    .findByNameContainingIgnoreCaseAndDeletedFalse(keyword, pageable);
        }

        return PageResponse.<ProductResponse>builder()
                .content(productPage.getContent()
                        .stream()
                        .map(productMapper::toResponse)
                        .toList())
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    public ProductResponse getProductById(Long id) {

        Product product = findActiveProduct(id);

        return productMapper.toResponse(product);
    }

    @Transactional
    public void reduceStock(Product product, Integer quantity){

        if(product.getStock() < quantity){
            throw new InsufficientStockException("Insufficient stock");
        }

        product.setStock(product.getStock() - quantity);

        productRepository.save(product);

    }

    @Transactional
    public ProductResponse updateProduct(Long id,
                                         UpdateProductRequest request) {

        Product product = findActiveProduct(id);

        log.info("Updating Product : {}", id);

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product updatedProduct = productRepository.save(product);

        log.info("Product Updated Successfully : {}", id);

        return productMapper.toResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {

        Product product = findActiveProduct(id);

        log.info("Soft Deleting Product : {}", id);

        product.setDeleted(true);

        productRepository.save(product);

        log.info("Product Soft Deleted Successfully : {}", id);
    }
    private Product findActiveProduct(Long id) {

        return productRepository.findById(id)
                .filter(product -> !product.isDeleted())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id : " + id));
    }
}
