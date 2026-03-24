-- V004__add_favoritos_table.sql
-- Tabla para almacenar favoritos (relación muchos-a-muchos entre usuarios y recetas)

CREATE TABLE IF NOT EXISTS favoritos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    receta_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_favorito_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorito_receta FOREIGN KEY (receta_id) 
        REFERENCES recetas(id) ON DELETE CASCADE,
    
    CONSTRAINT uk_favorito_usuario_receta UNIQUE (usuario_id, receta_id),
    
    INDEX idx_usuario (usuario_id),
    INDEX idx_receta (receta_id),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
