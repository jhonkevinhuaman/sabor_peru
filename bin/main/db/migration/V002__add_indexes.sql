-- V002__add_indexes.sql
-- Índices adicionales para optimización de performance
-- Se ejecuta después de V001__init_schema.sql

-- Índices para búsquedas y filtrado de recetas
CREATE INDEX IF NOT EXISTS idx_recetas_titulo ON recetas(titulo);
CREATE INDEX IF NOT EXISTS idx_recetas_nivel_dificultad ON recetas(nivel_dificultad);
CREATE INDEX IF NOT EXISTS idx_recetas_created_at ON recetas(created_at);
CREATE INDEX IF NOT EXISTS idx_recetas_cantidad_favoritos ON recetas(cantidad_favoritos DESC);

-- Índices para búsquedas de usuarios
CREATE INDEX IF NOT EXISTS idx_usuarios_nombre ON usuarios(nombre);
CREATE INDEX IF NOT EXISTS idx_usuarios_apellido ON usuarios(apellido);
CREATE INDEX IF NOT EXISTS idx_usuarios_es_admin ON usuarios(es_admin);
CREATE INDEX IF NOT EXISTS idx_usuarios_created_at ON usuarios(created_at);

-- Índices para notificaciones
CREATE INDEX IF NOT EXISTS idx_notificaciones_usuario_leida ON notificaciones(usuario_id, leida);
CREATE INDEX IF NOT EXISTS idx_notificaciones_created_at ON notificaciones(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notificaciones_tipo ON notificaciones(tipo);

-- Índices para auth tokens (seguridad)
CREATE INDEX IF NOT EXISTS idx_auth_tokens_jti ON auth_tokens(jti(255));
CREATE INDEX IF NOT EXISTS idx_auth_tokens_tipo ON auth_tokens(token_type);
