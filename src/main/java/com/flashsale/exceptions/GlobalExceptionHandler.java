package com.flashsale.exceptions;

import com.flashsale.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {
            NoStockRemaining.class,
            QuantityLimitExceeded.class
    })
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        String message = e.getMessage();
        ApiErrorResponse err =  new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                message,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(err, HttpStatus.CONFLICT);

    }

    @ExceptionHandler(
            value = {
                    MethodArgumentNotValidException.class,
                    DropsNotFoundException.class,
                    IllegalArgumentException.class
            }
    )
    public ResponseEntity<ApiErrorResponse> handleMethodArgsNotValidException(Exception e) {
        String message = e.getMessage();
        ApiErrorResponse err =  new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);

    }
}
