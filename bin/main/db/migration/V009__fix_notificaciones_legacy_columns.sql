-- Alinea notificaciones legacy con entidad JPA actual.
-- Permite inserts sin columna titulo (no usada por la entidad).

SET @db_name := DATABASE();

SET @has_titulo := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = @db_name
      AND table_name = 'notificaciones'
      AND column_name = 'titulo'
);

SET @sql := IF(
    @has_titulo = 1,
    'ALTER TABLE notificaciones MODIFY COLUMN titulo VARCHAR(255) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
