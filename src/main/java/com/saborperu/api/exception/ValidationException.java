package com.saborperu.api.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Excepción para validación de datos fallida
 */
public class ValidationException extends BusinessException {
    private final Map<String, String> errors;
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.errors = new HashMap<>();
    }
    
    public ValidationException(String message, Map<String, String> errors) {
        super(message, "VALIDATION_ERROR");
        this.errors = errors;
    }
    
    public ValidationException addError(String field, String error) {
        this.errors.put(field, error);
        return this;
    }
    
    public Map<String, String> getErrors() {
        return errors;
    }
}
