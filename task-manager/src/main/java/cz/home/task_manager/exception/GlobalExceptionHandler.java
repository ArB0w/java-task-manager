package cz.home.task_manager.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(
            ResponseStatusException exception) {

        Map<String, String> error = new HashMap<>();

        error.put("error", exception.getReason());

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJson(
            HttpMessageNotReadableException exception) {

        Map<String, String> error = new HashMap<>();

        error.put("error", "Invalid JSON request");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {

        Map<String, String> error = new HashMap<>();
        error.put("error", "HTTP method not supported");

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error);
    }
}
