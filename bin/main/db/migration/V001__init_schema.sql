-- V001__init_schema.sql
-- Inicialización del esquema de base de datos para SaborPerú
-- Define todas las tablas principales del sistema

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    correo VARCHAR(255) NOT NULL UNIQUE,
    contraseña_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    bio TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    es_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_correo (correo),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Recetas
CREATE TABLE IF NOT EXISTS recetas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tiempo_preparacion INT,
    porciones INT,
    nivel_dificultad VARCHAR(50),
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    usuario_creador_id BIGINT NOT NULL,
    validado_por_id BIGINT,
    cantidad_favoritos INT DEFAULT 0,
    motivo_rechazo TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_receta_usuario_creador FOREIGN KEY (usuario_creador_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_receta_validador FOREIGN KEY (validado_por_id) 
        REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_estado (estado),
    INDEX idx_usuario_creador (usuario_creador_id),
    INDEX idx_validador (validado_por_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Ingredientes
CREATE TABLE IF NOT EXISTS ingredientes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    cantidad DOUBLE NOT NULL,
    unidad VARCHAR(50) NOT NULL,
    instruccion_preparacion TEXT,
    receta_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ingrediente_receta FOREIGN KEY (receta_id) 
        REFERENCES recetas(id) ON DELETE CASCADE,
    INDEX idx_receta (receta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Pasos
CREATE TABLE IF NOT EXISTS pasos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    numero_paso INT NOT NULL,
    descripcion TEXT NOT NULL,
    tiempo_estimado INT,
    receta_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_paso_receta FOREIGN KEY (receta_id) 
        REFERENCES recetas(id) ON DELETE CASCADE,
    INDEX idx_receta (receta_id),
    UNIQUE KEY uk_receta_numero (receta_id, numero_paso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Notificaciones
CREATE TABLE IF NOT EXISTS notificaciones (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(100) NOT NULL,
    contenido TEXT,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    usuario_id BIGINT,
    receta_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_receta FOREIGN KEY (receta_id) 
        REFERENCES recetas(id) ON DELETE SET NULL,
    INDEX idx_usuario (usuario_id),
    INDEX idx_receta (receta_id),
    INDEX idx_leida (leida)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Auth Tokens
CREATE TABLE IF NOT EXISTS auth_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_type VARCHAR(50),
    jti TEXT,
    usuario_id BIGINT,
    expira_en BIGINT,
    revocado BOOLEAN DEFAULT FALSE,
    revocado_en BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_token_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_usuario (usuario_id),
    INDEX idx_revocado (revocado),
    INDEX idx_expira_en (expira_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
