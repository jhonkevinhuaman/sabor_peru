package com.saborperu.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Wrapper estándar para todas las respuestas HTTP de la API
 * Garantiza consistencia en el formato de respuesta
 *
 * Estructura:
 * {
 *   "success": true/false,
 *   "data": {...},                      // Null si error
 *   "message": "Mensaje de respuesta",
 *   "timestamp": "2026-03-23T12:34:56"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * Indica si la operación fue exitosa
     */
    private Boolean success;
    
    /**
     * Datos de la respuesta (null en caso de error)
     */
    private T data;
    
    /**
     * Mensaje descriptivo (éxito o error)
     */
    private String message;
    
    /**
     * Timestamp de la respuesta
     */
    private String timestamp;
    
    /**
     * Código de error (para debugging)
     */
    private String errorCode;
    
    /**
     * Factory method para respuesta exitosa
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
    
    /**
     * Factory method para respuesta sin datos
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(null, message);
    }
    
    /**
     * Factory method para error
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
    
    /**
     * Factory method para error sin código específico
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(message, "UNKNOWN_ERROR");
    }
}
