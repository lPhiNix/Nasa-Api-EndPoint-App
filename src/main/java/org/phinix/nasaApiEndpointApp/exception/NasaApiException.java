package org.phinix.nasaApiEndpointApp.exception;

/**
 * Custom unchecked exception for handling errors related to NASA API interactions.
 *
 * <p>This exception is used to wrap and signal any issues encountered during
 * communication with the external NASA NEO (Near Earth Object) API,
 * such as parsing failures or invalid responses.</p>
 *
 * <p>It extends {@link RuntimeException} to avoid the need for explicit
 * exception handling in service or controller layers, making it more
 * flexible in a Spring Boot environment where many exceptions are
 * handled globally via {@code @ControllerAdvice}.</p>
 *
 * <p>This contributes to clean architecture by isolating domain and
 * service logic from low-level technical concerns.</p>
 */
public class NasaApiException extends RuntimeException {
    /**
     * Constructs a new NasaApiException with the specified detail message.
     *
     * @param message the detail message describing the exception
     */
    public NasaApiException(String message) {
        super(message);
    }
    /**
     * Constructs a new NasaApiException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause of the exception (e.g., JSON parsing issue)
     */
    public NasaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
