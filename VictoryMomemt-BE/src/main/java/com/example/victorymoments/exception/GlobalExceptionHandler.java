package com.example.victorymoments.exception;

import com.example.victorymoments.exception.BaseException; // Import BaseException
import com.example.victorymoments.response.ApiErrorResponse; // Import ApiErrorResponse
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder; // Import này để lấy locale
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice; // Sử dụng RestControllerAdvice thay vì ControllerAdvice
import org.springframework.web.context.request.WebRequest; // Import này

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger; // Sử dụng SLF4J cho logging
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // 1. Xử lý lỗi validation DTO (từ @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
                .filter(error -> error instanceof FieldError) // Chỉ lấy field errors
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> messageSource.getMessage(error.getDefaultMessage(), null, error.getDefaultMessage(), currentLocale)
                ));

        // Log chi tiết lỗi validation cho developer (F12 trên server)
        logger.warn("Validation failed for request to {}. Errors: {}", request.getDescription(false), errors);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message(messageSource.getMessage("validation.failed", null, currentLocale)) // Thông báo chung cho validation
                .errors(errors) // Chi tiết lỗi từng trường
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 2. Xử lý các Custom Exceptions kế thừa từ BaseException
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        // Lấy message đã được localize từ MessageSource bằng messageKey và args
        String localizedMessage = messageSource.getMessage(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey(), currentLocale);

        // Log lỗi chi tiết cho developer (F12 trên server), bao gồm stack trace nếu lỗi là nghiêm trọng
        if (ex.getStatus().is5xxServerError() || ex.getCause() != null) {
            logger.error("Error occurred [{}]: {} - Path: {}", ex.getErrorCode(), localizedMessage, request.getDescription(false), ex); // ex sẽ in ra stack trace
        } else {
            logger.warn("Client error occurred [{}]: {} - Path: {}", ex.getErrorCode(), localizedMessage, request.getDescription(false));
        }

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(localizedMessage)
                .errors(null) // Các lỗi này không có field errors cụ thể
                .build();

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    // 3. Xử lý Exception chung không xác định (Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.unknown", null, "An unknown error occurred.", currentLocale);

        // Luôn log stack trace cho lỗi nội bộ của server
        logger.error("An unexpected error occurred: {} - Path: {}", ex.getMessage(), request.getDescription(false), ex); // ex sẽ in ra stack trace

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .errorCode("GENERIC_ERROR")
                .message(errorMessage)
                .errors(null)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}