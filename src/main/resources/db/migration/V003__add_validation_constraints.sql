-- V003__add_validation_constraints.sql
-- Constraints de validación adicionales

-- Validación de emails válidos (opcional, depende de BD)
-- ALTER TABLE usuarios ADD CONSTRAINT ck_email_format CHECK (correo REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$');

-- Validación de estados de receta
ALTER TABLE recetas ADD CONSTRAINT ck_receta_estado 
    CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA'));

-- Validación de estados de usuario
ALTER TABLE usuarios ADD CONSTRAINT ck_usuario_estado 
    CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO'));

-- Validación de tipos de notificación
ALTER TABLE notificaciones ADD CONSTRAINT ck_notif_tipo 
    CHECK (tipo IN ('RECETA_APROBADA', 'RECETA_RECHAZADA', 'NUEVO_FAVORITO', 'COMENTARIO', 'SISTEMA'));

-- Validación de niveles de dificultad
ALTER TABLE recetas ADD CONSTRAINT ck_nivel_dificultad 
    CHECK (nivel_dificultad IN ('FACIL', 'MEDIA', 'DIFICIL'));
