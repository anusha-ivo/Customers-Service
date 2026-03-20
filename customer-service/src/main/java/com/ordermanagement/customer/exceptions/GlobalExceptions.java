package com.ordermanagement.customer.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptions {

        private static final String SOURCE_APP = "CUSTOMER SERVICE";//it can be available in all method

        private Map<String, Object> buildError(
                String label,
                HttpStatus status,
                String message,
                String level,
                String severity) {

            return Map.of(
                    "label", label,
                    "code", String.valueOf(status.value()),
                    "level", level,
                    "severity", severity,
                    "message", message,
                    "httpStatus", status.name(),
                    "sourceApplication", SOURCE_APP
            );
        }
        @ExceptionHandler(CustomerException.class)
        public ResponseEntity<?> handleAppException(CustomerException ex) {

            HttpStatus status = ex.getStatus();

            return new ResponseEntity<>(
                    buildError(
                            ex.getLabel(),
                            status,
                            ex.getMessage(),
                            "REQUEST",
                            "NONFATAL"
                    ),
                    status
            );
        }
        @ExceptionHandler(MethodArgumentNotValidException.class)//when field errror happens
        public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {//call this exception

            String message = ex.getBindingResult()
                    .getFieldError()//get field
                    .getDefaultMessage();

            return ResponseEntity.badRequest().body(//fix to bad request
                    buildError("VALIDATION_ERROR",
                            HttpStatus.BAD_REQUEST,
                            message,
                            "REQUEST",
                            "NONFATAL"
                    )
            );
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        ex.printStackTrace();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(
                buildError(
                        "Internal Server Error",
                        status,
                        "Something went wrong",
                        "SYSTEM",
                        "FATAL"
                ),
                status
        );
    }
    }

