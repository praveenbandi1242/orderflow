package com.orderflow.controller;

import com.orderflow.dto.CreateProductRequest;
import com.orderflow.dto.ProductResponse;
import com.orderflow.dto.UpdateProductRequest;
import com.orderflow.dto.common.ApiResponse;
import com.orderflow.dto.common.PageResponse;
import com.orderflow.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "Product APIs",
        description = "Operations related to Products"
)

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Create Product",
            description = "Creates a new product"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        ProductResponse response = productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product created successfully")
                        .timestamp(LocalDateTime.now())
                        .data(response)
                        .build());
    }

    @Operation(
            summary = "Get Products",
            description = "Returns paginated list of products"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String keyword,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "5") int size,

            @RequestParam(defaultValue = "id") String sortBy,

            @RequestParam(defaultValue = "asc") String direction) {

        PageResponse<ProductResponse> response =
                productService.getProducts(keyword, page, size, sortBy, direction);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Products fetched successfully")
                        .timestamp(LocalDateTime.now())
                        .data(response)
                        .build()
        );
    }

    @Operation(
            summary = "Get Product",
            description = "Returns product by id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id) {

        ProductResponse response = productService.getProductById(id);

        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product fetched successfully")
                        .timestamp(LocalDateTime.now())
                        .data(response)
                        .build()
        );
    }

    @Operation(
            summary = "Update Product",
            description = "Updates an existing product"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(

            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateProductRequest request) {

        ProductResponse response =
                productService.updateProduct(id, request);

        return ResponseEntity.ok(

                ApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product updated successfully")
                        .timestamp(LocalDateTime.now())
                        .data(response)
                        .build()

        );
    }

    @Operation(
            summary = "Delete Product",
            description = "Soft deletes a product"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Product deleted successfully")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}
