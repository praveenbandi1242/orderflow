package com.orderflow.exception;

import com.orderflow.dto.common.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleResourceNotFoundException() {

        ResourceNotFoundException ex =
                new ResourceNotFoundException("Product not found");

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Product not found", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleInsufficientStockException() {

        InsufficientStockException ex =
                new InsufficientStockException("Insufficient stock");

        ResponseEntity<ErrorResponse> response =
                handler.handleStock(ex);

        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Insufficient stock", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleValidationException() throws Exception {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");

        bindingResult.addError(
                new FieldError("object",
                        "name",
                        "Name is required"));

        bindingResult.addError(
                new FieldError("object",
                        "price",
                        "Price must be greater than 0"));

        Method method = DummyController.class
                .getMethod("dummyMethod", String.class);

        MethodParameter parameter =
                new MethodParameter(method, 0);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(ex);

        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Validation failed",
                response.getBody().getMessage());

        assertEquals("Name is required",
                response.getBody().getErrors().get("name"));

        assertEquals("Price must be greater than 0",
                response.getBody().getErrors().get("price"));
    }

    @Test
    void shouldHandleGenericException() {

        Exception ex = new Exception("Unexpected");

        ResponseEntity<ErrorResponse> response =
                handler.handleException(ex);

        assertEquals(500, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Something went wrong",
                response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    static class DummyController {
        public void dummyMethod(String value) {
        }
    }
}