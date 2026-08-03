-- Compatibiliza la estructura heredada de pacientes
-- con el nuevo módulo administrativo.

-- Las columnas antiguas de apellidos dejan de ser obligatorias.
-- El nuevo modelo utiliza la columna unificada "apellidos".
ALTER TABLE pacientes
    ALTER COLUMN apellido_paterno
        DROP NOT NULL;

-- Mantiene apellido_materno como dato heredado opcional.
ALTER TABLE pacientes
    ALTER COLUMN apellido_materno
        DROP NOT NULL;

-- La auditoría técnica será implementada mediante
-- los identificadores obtenidos del JWT.
-- Temporalmente permite registros sin estos campos heredados.
ALTER TABLE pacientes
    ALTER COLUMN creado_por
        DROP NOT NULL;

ALTER TABLE pacientes
    ALTER COLUMN modificado_por
        DROP NOT NULL;

-- Copia los apellidos heredados al nuevo campo unificado.
UPDATE pacientes
SET apellidos =
        TRIM(
            CONCAT_WS(
                ' ',
                NULLIF(TRIM(apellido_paterno), ''),
                NULLIF(TRIM(apellido_materno), '')
            )
        )
WHERE (
        apellidos IS NULL
        OR TRIM(apellidos) = ''
      )
  AND (
        apellido_paterno IS NOT NULL
        OR apellido_materno IS NOT NULL
      );

-- Completa únicamente registros antiguos sin apellidos.
UPDATE pacientes
SET apellidos = 'Por completar'
WHERE apellidos IS NULL
   OR TRIM(apellidos) = '';

-- Copia el contacto de emergencia anterior al nuevo modelo.
UPDATE pacientes
SET contacto_emergencia =
        contacto_emergencia_nombre
WHERE (
        contacto_emergencia IS NULL
        OR TRIM(contacto_emergencia) = ''
      )
  AND contacto_emergencia_nombre IS NOT NULL
  AND TRIM(contacto_emergencia_nombre) <> '';

-- Copia el teléfono de emergencia anterior al nuevo modelo.
UPDATE pacientes
SET telefono_emergencia =
        contacto_emergencia_telefono
WHERE (
        telefono_emergencia IS NULL
        OR TRIM(telefono_emergencia) = ''
      )
  AND contacto_emergencia_telefono IS NOT NULL
  AND TRIM(contacto_emergencia_telefono) <> '';

-- Garantiza que el nuevo campo de apellidos siga siendo obligatorio.
ALTER TABLE pacientes
    ALTER COLUMN apellidos
        SET NOT NULL;

COMMENT ON COLUMN pacientes.apellidos IS
    'Apellidos unificados utilizados por el módulo actual.';

COMMENT ON COLUMN pacientes.apellido_paterno IS
    'Campo heredado conservado para compatibilidad histórica.';

COMMENT ON COLUMN pacientes.apellido_materno IS
    'Campo heredado conservado para compatibilidad histórica.';

COMMENT ON COLUMN pacientes.creado_por IS
    'Usuario creador heredado; la auditoría JWT se completará posteriormente.';

COMMENT ON COLUMN pacientes.modificado_por IS
    'Usuario modificador heredado; la auditoría JWT se completará posteriormente.';