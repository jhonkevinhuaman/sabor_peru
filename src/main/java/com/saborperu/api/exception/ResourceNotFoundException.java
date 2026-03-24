package com.saborperu.api.exception;

/**
 * Excepción para recursos no encontrados (404)
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s con ID %d no encontrado", resourceName, id), 
              "RESOURCE_NOT_FOUND");
    }
    
    public ResourceNotFoundException(String resourceName, String value) {
        super(String.format("%s con valor '%s' no encontrado", resourceName, value), 
              "RESOURCE_NOT_FOUND");
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}
