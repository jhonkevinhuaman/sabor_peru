package com.saborperu.api.exception;

/**
 * Excepción para acceso no autorizado (401/403)
 */
public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }
    
    public UnauthorizedException() {
        super("Autenticación requerida", "UNAUTHORIZED");
    }
    
    public static class InvalidCredentialsException extends UnauthorizedException {
        public InvalidCredentialsException() {
            super("Correo o contraseña incorrectos");
        }
    }
    
    public static class TokenExpiredException extends UnauthorizedException {
        public TokenExpiredException() {
            super("Token JWT expirado");
        }
    }
    
    public static class InvalidTokenException extends UnauthorizedException {
        public InvalidTokenException() {
            super("Token JWT inválido");
        }
    }
}
