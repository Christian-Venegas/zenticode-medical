-- Refuerza la auditoría y consistencia de diagnósticos clínicos.

-- Añade trazabilidad para futuras modificaciones.
ALTER TABLE diagnosticos_consultas
    ADD COLUMN IF NOT EXISTS fecha_modificacion
        TIMESTAMPTZ;

ALTER TABLE diagnosticos_consultas
    ADD COLUMN IF NOT EXISTS modificado_por
        BIGINT;

-- Añade trazabilidad para desactivaciones.
ALTER TABLE diagnosticos_consultas
    ADD COLUMN IF NOT EXISTS motivo_desactivacion
        VARCHAR(500);

ALTER TABLE diagnosticos_consultas
    ADD COLUMN IF NOT EXISTS fecha_desactivacion
        TIMESTAMPTZ;

ALTER TABLE diagnosticos_consultas
    ADD COLUMN IF NOT EXISTS desactivado_por
        BIGINT;

-- Completa auditoría de registros anteriores.
UPDATE diagnosticos_consultas
SET fecha_modificacion = fecha_registro
WHERE fecha_modificacion IS NULL;

UPDATE diagnosticos_consultas
SET modificado_por = registrado_por
WHERE modificado_por IS NULL;

-- Establece valores predeterminados para nuevos registros.
ALTER TABLE diagnosticos_consultas
    ALTER COLUMN fecha_modificacion
        SET DEFAULT CURRENT_TIMESTAMP;

-- La auditoría principal es obligatoria.
ALTER TABLE diagnosticos_consultas
    ALTER COLUMN fecha_modificacion
        SET NOT NULL;

ALTER TABLE diagnosticos_consultas
    ALTER COLUMN modificado_por
        SET NOT NULL;

-- Vincula los usuarios responsables existentes.
ALTER TABLE diagnosticos_consultas
    ADD CONSTRAINT fk_diagnosticos_modificado_por
        FOREIGN KEY (modificado_por)
        REFERENCES usuarios (id_usuarios);

ALTER TABLE diagnosticos_consultas
    ADD CONSTRAINT fk_diagnosticos_desactivado_por
        FOREIGN KEY (desactivado_por)
        REFERENCES usuarios (id_usuarios);

-- Si existieran varios principales antiguos,
-- conserva como principal únicamente el primero.
WITH principales_duplicados AS (
    SELECT
        id_diagnosticos_consultas,
        ROW_NUMBER() OVER (
            PARTITION BY
                id_consultorios,
                id_consultas
            ORDER BY
                fecha_registro ASC,
                id_diagnosticos_consultas ASC
        ) AS posicion
    FROM diagnosticos_consultas
    WHERE principal = TRUE
      AND estado = 'ACTIVO'
)
UPDATE diagnosticos_consultas diagnostico
SET principal = FALSE,
    fecha_modificacion = CURRENT_TIMESTAMP,
    modificado_por = registrado_por
FROM principales_duplicados duplicado
WHERE diagnostico.id_diagnosticos_consultas =
        duplicado.id_diagnosticos_consultas
  AND duplicado.posicion > 1;

-- Garantiza a nivel de PostgreSQL que exista como máximo
-- un diagnóstico principal activo por consulta.
CREATE UNIQUE INDEX IF NOT EXISTS
    ux_diagnosticos_consulta_principal_activo
ON diagnosticos_consultas (
    id_consultorios,
    id_consultas
)
WHERE principal = TRUE
  AND estado = 'ACTIVO';

-- Evita repetir un mismo CIE-10 activo dentro de la consulta.
CREATE UNIQUE INDEX IF NOT EXISTS
    ux_diagnosticos_consulta_cie10_activo
ON diagnosticos_consultas (
    id_consultorios,
    id_consultas,
    codigo_cie10
)
WHERE codigo_cie10 IS NOT NULL
  AND estado = 'ACTIVO';

-- Valida los tipos de diagnóstico permitidos.
ALTER TABLE diagnosticos_consultas
    DROP CONSTRAINT IF EXISTS
        ck_diagnosticos_tipo;

ALTER TABLE diagnosticos_consultas
    ADD CONSTRAINT ck_diagnosticos_tipo
        CHECK (
            tipo IN (
                'PRESUNTIVO',
                'DEFINITIVO'
            )
        );

-- Valida los estados lógicos permitidos.
ALTER TABLE diagnosticos_consultas
    DROP CONSTRAINT IF EXISTS
        ck_diagnosticos_estado;

ALTER TABLE diagnosticos_consultas
    ADD CONSTRAINT ck_diagnosticos_estado
        CHECK (
            estado IN (
                'ACTIVO',
                'INACTIVO'
            )
        );

-- Si está inactivo, no puede continuar como principal.
ALTER TABLE diagnosticos_consultas
    DROP CONSTRAINT IF EXISTS
        ck_diagnosticos_inactivo_no_principal;

ALTER TABLE diagnosticos_consultas
    ADD CONSTRAINT ck_diagnosticos_inactivo_no_principal
        CHECK (
            estado <> 'INACTIVO'
            OR principal = FALSE
        );

-- La desactivación debe conservar responsable, fecha y motivo.
ALTER TABLE diagnosticos_consultas
    DROP CONSTRAINT IF EXISTS
        ck_diagnosticos_auditoria_desactivacion;

ALTER TABLE diagnosticos_consultas
    ADD CONSTRAINT ck_diagnosticos_auditoria_desactivacion
        CHECK (
            (
                estado = 'ACTIVO'
                AND motivo_desactivacion IS NULL
                AND fecha_desactivacion IS NULL
                AND desactivado_por IS NULL
            )
            OR
            (
                estado = 'INACTIVO'
                AND motivo_desactivacion IS NOT NULL
                AND LENGTH(
                    TRIM(motivo_desactivacion)
                ) BETWEEN 5 AND 500
                AND fecha_desactivacion IS NOT NULL
                AND desactivado_por IS NOT NULL
            )
        );

COMMENT ON COLUMN diagnosticos_consultas.fecha_modificacion IS
    'Fecha de la última modificación del diagnóstico.';

COMMENT ON COLUMN diagnosticos_consultas.modificado_por IS
    'Usuario responsable de la última modificación.';

COMMENT ON COLUMN diagnosticos_consultas.motivo_desactivacion IS
    'Justificación obligatoria para desactivar el diagnóstico.';

COMMENT ON COLUMN diagnosticos_consultas.fecha_desactivacion IS
    'Fecha en que el diagnóstico fue desactivado.';

COMMENT ON COLUMN diagnosticos_consultas.desactivado_por IS
    'Usuario responsable de desactivar el diagnóstico.';