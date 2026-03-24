-- V006__sanitize_zero_dates.sql
-- Corrige registros legacy con fecha cero que fallan con SQL strict mode.

-- ingredientes.fecha_creacion
SET @sql = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'ingredientes'
          AND column_name = 'fecha_creacion'
    ),
    "UPDATE ingredientes SET fecha_creacion = '1970-01-01 00:00:01' WHERE fecha_creacion = '0000-00-00 00:00:00'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ingredientes.fecha_actualizacion
SET @sql = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'ingredientes'
          AND column_name = 'fecha_actualizacion'
    ),
    "UPDATE ingredientes SET fecha_actualizacion = '1970-01-01 00:00:01' WHERE fecha_actualizacion = '0000-00-00 00:00:00'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- pasos.fecha_creacion
SET @sql = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'pasos'
          AND column_name = 'fecha_creacion'
    ),
    "UPDATE pasos SET fecha_creacion = '1970-01-01 00:00:01' WHERE fecha_creacion = '0000-00-00 00:00:00'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- pasos.fecha_actualizacion
SET @sql = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'pasos'
          AND column_name = 'fecha_actualizacion'
    ),
    "UPDATE pasos SET fecha_actualizacion = '1970-01-01 00:00:01' WHERE fecha_actualizacion = '0000-00-00 00:00:00'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- recetas.fecha_creacion
SET @sql = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'recetas'
          AND column_name = 'fecha_creacion'
    ),
    "UPDATE recetas SET fecha_creacion = '1970-01-01 00:00:01' WHERE fecha_creacion = '0000-00-00 00:00:00'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- recetas.fecha_actualizacion
SET @sql = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'recetas'
          AND column_name = 'fecha_actualizacion'
    ),
    "UPDATE recetas SET fecha_actualizacion = '1970-01-01 00:00:01' WHERE fecha_actualizacion = '0000-00-00 00:00:00'",
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
