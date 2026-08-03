-- Añade el estado activo a los roles existentes.
ALTER TABLE roles
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;

-- Documenta la finalidad de la columna.
COMMENT ON COLUMN roles.activo IS
    'Indica si el rol puede seguir siendo asignado a usuarios.';