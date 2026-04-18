package com.bookvault.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Uniform wrapper for all API responses.
 *
 * @param <T> the type of the response payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a successful response with a data payload.
     *
     * @param data    the response data
     * @param message a human-readable success message
     * @param <T>     the payload type
     * @return a success response
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null);
    }

    /**
     * Creates a successful response with the default "OK" message.
     *
     * @param data the response data
     * @param <T>  the payload type
     * @return a success response
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    /**
     * Creates an error response without a data payload.
     *
     * @param message   the error description
     * @param errorCode the machine-readable error code
     * @param <T>       the payload type
     * @return an error response
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode);
    }

    /**
     * Creates a validation error response with per-field error details.
     *
     * @param message the top-level error message
     * @param data    the field error map
     * @param <T>     the payload type
     * @return a validation error response
     */
    public static <T> ApiResponse<T> validationError(String message, T data) {
        return new ApiResponse<>(false, message, data, "VALIDATION_ERROR");
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
