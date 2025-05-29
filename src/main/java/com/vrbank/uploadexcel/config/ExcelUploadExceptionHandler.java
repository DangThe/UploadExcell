package com.vrbank.uploadexcel.config;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Exception Handler Configuration
 */
@ControllerAdvice
public class ExcelUploadExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ExcelUploadExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        log.error("File size exceeds maximum allowed size", exc);
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "message", "File size exceeds maximum allowed size (50MB)", "error", "MAX_FILE_SIZE_EXCEEDED"));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException exc) {
        log.error("IO error during file processing", exc);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            Map.of("success", false, "message", "Error reading file: " + exc.getMessage(), "error", "FILE_READ_ERROR")
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ConstraintViolationException exc) {
        log.error("Validation error", exc);
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "message", "Validation error: " + exc.getMessage(), "error", "VALIDATION_ERROR"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException exc) {
        log.error("Runtime error during excel upload", exc);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            Map.of("success", false, "message", "Internal error: " + exc.getMessage(), "error", "INTERNAL_ERROR")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception exc) {
        log.error("Unexpected error during excel upload", exc);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            Map.of("success", false, "message", "Unexpected error occurred", "error", "UNEXPECTED_ERROR")
        );
    }
}
