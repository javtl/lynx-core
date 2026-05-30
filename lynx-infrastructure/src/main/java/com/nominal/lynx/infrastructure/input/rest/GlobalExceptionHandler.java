package com.nominal.lynx.infrastructure.input.rest;

import com.nominal.lynx.domain.model.InvalidStateTransitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — Mapea excepciones a respuestas HTTP limpias 🎯
 *
 * @RestControllerAdvice: Intercepta excepciones de TODOS los controllers
 * Convierte excepciones del dominio en respuestas HTTP profesionales.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura: InvalidStateTransitionException → 409 Conflict
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransition(
            InvalidStateTransitionException ex,
            WebRequest request) {

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                409,
                "INVALID_STATE_TRANSITION",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Captura: MethodArgumentNotValidException → 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                400,
                "VALIDATION_ERROR",
                "Validación fallida",
                errors.toString()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Captura: RuntimeException (genérica) → 500 Internal Server Error
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                500,
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * ErrorResponse — Formato estándar de error
     */
    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}
}