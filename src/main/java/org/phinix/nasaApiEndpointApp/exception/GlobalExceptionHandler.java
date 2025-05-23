package org.phinix.nasaApiEndpointApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the NASA API application.
 *
 * <p>Handles custom and generic exceptions gracefully, returning
 * meaningful HTTP status codes and structured error responses.</p>
 *
 * <p>Architecture-wise, this follows the Separation of Concerns (SoC)
 * by isolating exception logic from business and controller layers.</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles custom exceptions related to NASA API errors.
     *
     * @param ex the custom exception
     * @return standardized error response with 502 Bad Gateway
     */
    @ExceptionHandler(NasaApiException.class)
    public ResponseEntity<?> handleNasaApiException(NasaApiException ex) {
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    /**
     * Handles illegal argument exceptions, such as invalid query parameters.
     *
     * @param ex the exception
     * @return standardized error response with 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Catches all other unexpected exceptions.
     *
     * @param ex the exception
     * @return standardized error response with 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());
    }

    /**
     * Helper method to build a consistent JSON error response body.
     *
     * @param status  the HTTP status to return
     * @param message the error message
     * @return a ResponseEntity containing a structured error response
     */
    private ResponseEntity<?> buildResponse(HttpStatus status, String message) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);
        return new ResponseEntity<>(errorDetails, status);
    }
}
