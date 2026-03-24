-- Alinea esquema legacy con BaseEntity para la tabla ingredientes.
-- Agrega columnas de auditoria faltantes y asegura valores no nulos.

SET @db_name := DATABASE();

SET @has_fecha_creacion := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = @db_name
      AND table_name = 'ingredientes'
      AND column_name = 'fecha_creacion'
);

SET @sql := IF(
    @has_fecha_creacion = 0,
    'ALTER TABLE ingredientes ADD COLUMN fecha_creacion DATETIME(6) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_fecha_actualizacion := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = @db_name
      AND table_name = 'ingredientes'
      AND column_name = 'fecha_actualizacion'
);

SET @sql := IF(
    @has_fecha_actualizacion = 0,
    'ALTER TABLE ingredientes ADD COLUMN fecha_actualizacion DATETIME(6) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE ingredientes
SET fecha_creacion = COALESCE(fecha_creacion, NOW(6)),
    fecha_actualizacion = COALESCE(fecha_actualizacion, NOW(6));

ALTER TABLE ingredientes
    MODIFY COLUMN fecha_creacion DATETIME(6) NOT NULL,
    MODIFY COLUMN fecha_actualizacion DATETIME(6) NOT NULL;
