package com.crud.project.shoppiq.exceptions;

import com.crud.project.shoppiq.auth.controller.AuthController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<String> handleItemNotFoundException(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ItemDetailsNotFoundException.class)
    public ResponseEntity<String> handleItemDetailsNotFoundException(ItemDetailsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ItemReviewNotFoundException.class)
    public ResponseEntity<String> handleItemReviewNotFoundException(ItemReviewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleOrderNotFoundException(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    /**
     * Handles validation exceptions thrown when @Valid fails on request bodies.
     * Extracts field error messages and returns them as plain text.
     *
     * <p>This handler catches MethodArgumentNotValidException before Spring's
     * default error handler, allowing us to return simple text messages
     * instead of the default JSON error response.</p>
     *
     * @param ex the validation exception containing field errors
     * @return 400 with field error messages as plain text
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(". "));

        logger.debug("Validation failed: {}", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }
}
