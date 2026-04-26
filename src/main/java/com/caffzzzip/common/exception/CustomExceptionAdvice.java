package com.caffzzzip.common.exception;

import com.caffzzzip.common.error.ErrorCode;
import com.caffzzzip.common.template.ApiResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@Component
@RequiredArgsConstructor
public class CustomExceptionAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResTemplate<?> handleServerException(final Exception e) {
        log.error("Internal Server Error: {}", e.getMessage(), e);

        return ApiResTemplate.errorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                e.getMessage()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResTemplate<?>> handleCustomException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage(), e);

        ApiResTemplate<?> body = ApiResTemplate.errorResponse(
                e.getErrorCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(body);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResTemplate<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errorMap = new HashMap<>();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ApiResTemplate.errorResponse(
                ErrorCode.VALIDATION_ERROR,
                convertMapToString(errorMap)
        );
    }

    private String convertMapToString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue())
                    .append(", ");
        }

        if (!sb.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }
}