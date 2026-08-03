-- =========================================================
-- ZENTICODE MEDICAL
-- Evolución segura de historias clínicas
--
-- Esta migración conserva la tabla y los registros
-- creados antes de incorporar Flyway al módulo clínico.
-- =========================================================

-- Amplía grupo_sanguineo para admitir enums descriptivos.
ALTER TABLE historias_clinicas
    ALTER COLUMN grupo_sanguineo
    TYPE VARCHAR(20)
    USING grupo_sanguineo::VARCHAR(20);

-- Elimina temporalmente los CHECK antiguos relacionados
-- con grupo sanguíneo para permitir normalizar los valores.
DO $$
DECLARE
    restriccion RECORD;
BEGIN
    FOR restriccion IN
        SELECT
            conname
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND contype = 'c'
          AND pg_get_constraintdef(oid)
              ILIKE '%grupo_sanguineo%'
    LOOP
        EXECUTE FORMAT(
            'ALTER TABLE historias_clinicas '
            || 'DROP CONSTRAINT %I',
            restriccion.conname
        );
    END LOOP;
END
$$;

-- Convierte valores abreviados al enum definitivo.
UPDATE historias_clinicas
SET grupo_sanguineo =
    CASE UPPER(TRIM(grupo_sanguineo))
        WHEN 'A+' THEN 'A_POSITIVO'
        WHEN 'A POSITIVO' THEN 'A_POSITIVO'
        WHEN 'A_POSITIVO' THEN 'A_POSITIVO'

        WHEN 'A-' THEN 'A_NEGATIVO'
        WHEN 'A NEGATIVO' THEN 'A_NEGATIVO'
        WHEN 'A_NEGATIVO' THEN 'A_NEGATIVO'

        WHEN 'B+' THEN 'B_POSITIVO'
        WHEN 'B POSITIVO' THEN 'B_POSITIVO'
        WHEN 'B_POSITIVO' THEN 'B_POSITIVO'

        WHEN 'B-' THEN 'B_NEGATIVO'
        WHEN 'B NEGATIVO' THEN 'B_NEGATIVO'
        WHEN 'B_NEGATIVO' THEN 'B_NEGATIVO'

        WHEN 'AB+' THEN 'AB_POSITIVO'
        WHEN 'AB POSITIVO' THEN 'AB_POSITIVO'
        WHEN 'AB_POSITIVO' THEN 'AB_POSITIVO'

        WHEN 'AB-' THEN 'AB_NEGATIVO'
        WHEN 'AB NEGATIVO' THEN 'AB_NEGATIVO'
        WHEN 'AB_NEGATIVO' THEN 'AB_NEGATIVO'

        WHEN 'O+' THEN 'O_POSITIVO'
        WHEN 'O POSITIVO' THEN 'O_POSITIVO'
        WHEN 'O_POSITIVO' THEN 'O_POSITIVO'

        WHEN 'O-' THEN 'O_NEGATIVO'
        WHEN 'O NEGATIVO' THEN 'O_NEGATIVO'
        WHEN 'O_NEGATIVO' THEN 'O_NEGATIVO'

        ELSE 'DESCONOCIDO'
    END;

-- Evita valores nulos antes de aplicar restricciones.
UPDATE historias_clinicas
SET grupo_sanguineo = 'DESCONOCIDO'
WHERE grupo_sanguineo IS NULL
   OR TRIM(grupo_sanguineo) = '';

ALTER TABLE historias_clinicas
    ALTER COLUMN grupo_sanguineo
    SET DEFAULT 'DESCONOCIDO';

ALTER TABLE historias_clinicas
    ALTER COLUMN grupo_sanguineo
    SET NOT NULL;

-- Añade los campos de la nueva arquitectura.
ALTER TABLE historias_clinicas
    ADD COLUMN IF NOT EXISTS numero_historia
        VARCHAR(30);

ALTER TABLE historias_clinicas
    ADD COLUMN IF NOT EXISTS ocupacion
        VARCHAR(120);

ALTER TABLE historias_clinicas
    ADD COLUMN IF NOT EXISTS estado_civil
        VARCHAR(50);

ALTER TABLE historias_clinicas
    ADD COLUMN IF NOT EXISTS lugar_nacimiento
        VARCHAR(150);

-- Amplía observaciones para el límite del nuevo contrato.
ALTER TABLE historias_clinicas
    ALTER COLUMN observaciones_generales
    TYPE VARCHAR(2000)
    USING observaciones_generales::VARCHAR(2000);

-- Genera un número estable para las historias existentes.
UPDATE historias_clinicas
SET numero_historia =
    'HC-'
    || LPAD(
        id_historias_clinicas::TEXT,
        8,
        '0'
    )
WHERE numero_historia IS NULL
   OR TRIM(numero_historia) = '';

ALTER TABLE historias_clinicas
    ALTER COLUMN numero_historia
    SET NOT NULL;

-- Normaliza el estado existente.
UPDATE historias_clinicas
SET estado = UPPER(TRIM(estado));

UPDATE historias_clinicas
SET estado = 'ACTIVA'
WHERE estado IS NULL
   OR TRIM(estado) = ''
   OR estado NOT IN (
       'ACTIVA',
       'CERRADA'
   );

ALTER TABLE historias_clinicas
    ALTER COLUMN estado
    SET DEFAULT 'ACTIVA';

ALTER TABLE historias_clinicas
    ALTER COLUMN estado
    SET NOT NULL;

-- Garantiza las fechas administrativas.
UPDATE historias_clinicas
SET fecha_apertura = CURRENT_TIMESTAMP
WHERE fecha_apertura IS NULL;

UPDATE historias_clinicas
SET fecha_modificacion = CURRENT_TIMESTAMP
WHERE fecha_modificacion IS NULL;

ALTER TABLE historias_clinicas
    ALTER COLUMN fecha_apertura
    SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE historias_clinicas
    ALTER COLUMN fecha_apertura
    SET NOT NULL;

ALTER TABLE historias_clinicas
    ALTER COLUMN fecha_modificacion
    SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE historias_clinicas
    ALTER COLUMN fecha_modificacion
    SET NOT NULL;

-- Una historia por paciente dentro de cada consultorio.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND conname =
            'uk_historias_clinicas_consultorio_paciente'
    ) THEN
        ALTER TABLE historias_clinicas
            ADD CONSTRAINT
                uk_historias_clinicas_consultorio_paciente
            UNIQUE (
                id_consultorios,
                id_pacientes
            );
    END IF;
END
$$;

-- Número de historia único dentro del consultorio.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND conname =
            'uk_historias_clinicas_consultorio_numero'
    ) THEN
        ALTER TABLE historias_clinicas
            ADD CONSTRAINT
                uk_historias_clinicas_consultorio_numero
            UNIQUE (
                id_consultorios,
                numero_historia
            );
    END IF;
END
$$;

-- Restringe los grupos sanguíneos definitivos.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND conname =
            'ck_historias_clinicas_grupo_sanguineo'
    ) THEN
        ALTER TABLE historias_clinicas
            ADD CONSTRAINT
                ck_historias_clinicas_grupo_sanguineo
            CHECK (
                grupo_sanguineo IN (
                    'A_POSITIVO',
                    'A_NEGATIVO',
                    'B_POSITIVO',
                    'B_NEGATIVO',
                    'AB_POSITIVO',
                    'AB_NEGATIVO',
                    'O_POSITIVO',
                    'O_NEGATIVO',
                    'DESCONOCIDO'
                )
            );
    END IF;
END
$$;

-- Restringe los estados admitidos.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND conname =
            'ck_historias_clinicas_estado'
    ) THEN
        ALTER TABLE historias_clinicas
            ADD CONSTRAINT
                ck_historias_clinicas_estado
            CHECK (
                estado IN (
                    'ACTIVA',
                    'CERRADA'
                )
            );
    END IF;
END
$$;

-- Valida el número de historia.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND conname =
            'ck_historias_clinicas_numero_historia'
    ) THEN
        ALTER TABLE historias_clinicas
            ADD CONSTRAINT
                ck_historias_clinicas_numero_historia
            CHECK (
                LENGTH(TRIM(numero_historia))
                BETWEEN 1 AND 30
            );
    END IF;
END
$$;

-- Valida la ocupación opcional.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND conname =
            'ck_historias_clinicas_ocupacion'
    ) THEN
        ALTER TABLE historias_clinicas
            ADD CONSTRAINT
                ck_historias_clinicas_ocupacion
            CHECK (
                ocupacion IS NULL
                OR LENGTH(TRIM(ocupacion))
                    BETWEEN 1 AND 120
            );
    END IF;
END
$$;

-- Valida el estado civil opcional.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND conname =
            'ck_historias_clinicas_estado_civil'
    ) THEN
        ALTER TABLE historias_clinicas
            ADD CONSTRAINT
                ck_historias_clinicas_estado_civil
            CHECK (
                estado_civil IS NULL
                OR LENGTH(TRIM(estado_civil))
                    BETWEEN 1 AND 50
            );
    END IF;
END
$$;

-- Valida el lugar de nacimiento opcional.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND conname =
            'ck_historias_clinicas_lugar_nacimiento'
    ) THEN
        ALTER TABLE historias_clinicas
            ADD CONSTRAINT
                ck_historias_clinicas_lugar_nacimiento
            CHECK (
                lugar_nacimiento IS NULL
                OR LENGTH(TRIM(lugar_nacimiento))
                    BETWEEN 1 AND 150
            );
    END IF;
END
$$;

-- Valida las observaciones generales.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid =
            'public.historias_clinicas'::REGCLASS
          AND conname =
            'ck_historias_clinicas_observaciones'
    ) THEN
        ALTER TABLE historias_clinicas
            ADD CONSTRAINT
                ck_historias_clinicas_observaciones
            CHECK (
                observaciones_generales IS NULL
                OR LENGTH(
                    TRIM(observaciones_generales)
                ) BETWEEN 1 AND 2000
            );
    END IF;
END
$$;

-- Índices de acceso frecuente.
CREATE INDEX IF NOT EXISTS
    idx_historias_clinicas_id_pacientes
ON historias_clinicas (
    id_pacientes
);

CREATE INDEX IF NOT EXISTS
    idx_historias_clinicas_id_consultorios
ON historias_clinicas (
    id_consultorios
);

CREATE INDEX IF NOT EXISTS
    idx_historias_clinicas_consultorio_estado
ON historias_clinicas (
    id_consultorios,
    estado
);

COMMENT ON TABLE historias_clinicas IS
    'Historia clínica general de un paciente dentro de un consultorio.';

COMMENT ON COLUMN historias_clinicas.numero_historia IS
    'Número único de historia dentro del consultorio.';

COMMENT ON COLUMN historias_clinicas.grupo_sanguineo IS
    'Grupo sanguíneo conocido o DESCONOCIDO.';

COMMENT ON COLUMN historias_clinicas.estado IS
    'Estado lógico ACTIVA o CERRADA.';