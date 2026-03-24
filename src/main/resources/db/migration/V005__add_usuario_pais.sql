-- V005__add_usuario_pais.sql
-- Agrega el campo pais al perfil de usuario.

ALTER TABLE usuarios
    ADD pais VARCHAR(100) NULL;

CREATE INDEX idx_usuarios_pais ON usuarios(pais);
