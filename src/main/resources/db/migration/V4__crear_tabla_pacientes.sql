-- Completa de forma segura la tabla pacientes creada inicialmente.
-- La migración conserva cualquier estructura o información existente.

-- Añade las columnas administrativas que todavía no existan.
ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS tipo_documento VARCHAR(20);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS numero_documento VARCHAR(20);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS nombres VARCHAR(100);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS apellidos VARCHAR(100);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS fecha_nacimiento DATE;

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS telefono VARCHAR(20);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS correo VARCHAR(180);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS direccion VARCHAR(250);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS contacto_emergencia VARCHAR(150);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS telefono_emergencia VARCHAR(20);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20);

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS fecha_creacion TIMESTAMPTZ;

ALTER TABLE pacientes
    ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMPTZ;

-- Completa valores técnicos faltantes sin eliminar datos.
UPDATE pacientes
SET tipo_documento = 'OTRO'
WHERE tipo_documento IS NULL
   OR TRIM(tipo_documento) = '';

UPDATE pacientes
SET numero_documento =
        'SIN-DOCUMENTO-' || id_pacientes
WHERE numero_documento IS NULL
   OR TRIM(numero_documento) = '';

UPDATE pacientes
SET nombres = 'Por completar'
WHERE nombres IS NULL
   OR TRIM(nombres) = '';

UPDATE pacientes
SET apellidos = 'Por completar'
WHERE apellidos IS NULL
   OR TRIM(apellidos) = '';

UPDATE pacientes
SET estado = 'ACTIVO'
WHERE estado IS NULL
   OR TRIM(estado) = '';

UPDATE pacientes
SET fecha_creacion = CURRENT_TIMESTAMP
WHERE fecha_creacion IS NULL;

UPDATE pacientes
SET fecha_modificacion = CURRENT_TIMESTAMP
WHERE fecha_modificacion IS NULL;

-- Define valores predeterminados para nuevos registros.
ALTER TABLE pacientes
    ALTER COLUMN tipo_documento
        SET DEFAULT 'OTRO';

ALTER TABLE pacientes
    ALTER COLUMN estado
        SET DEFAULT 'ACTIVO';

ALTER TABLE pacientes
    ALTER COLUMN fecha_creacion
        SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE pacientes
    ALTER COLUMN fecha_modificacion
        SET DEFAULT CURRENT_TIMESTAMP;

-- Los datos mínimos del paciente son obligatorios.
ALTER TABLE pacientes
    ALTER COLUMN tipo_documento
        SET NOT NULL;

ALTER TABLE pacientes
    ALTER COLUMN numero_documento
        SET NOT NULL;

ALTER TABLE pacientes
    ALTER COLUMN nombres
        SET NOT NULL;

ALTER TABLE pacientes
    ALTER COLUMN apellidos
        SET NOT NULL;

ALTER TABLE pacientes
    ALTER COLUMN estado
        SET NOT NULL;

ALTER TABLE pacientes
    ALTER COLUMN fecha_creacion
        SET NOT NULL;

ALTER TABLE pacientes
    ALTER COLUMN fecha_modificacion
        SET NOT NULL;

-- Elimina restricciones anteriores con estos nombres si existen.
ALTER TABLE pacientes
    DROP CONSTRAINT IF EXISTS
        ck_pacientes_tipo_documento;

ALTER TABLE pacientes
    DROP CONSTRAINT IF EXISTS
        ck_pacientes_estado;

ALTER TABLE pacientes
    DROP CONSTRAINT IF EXISTS
        ck_pacientes_numero_documento;

ALTER TABLE pacientes
    DROP CONSTRAINT IF EXISTS
        ck_pacientes_nombres;

ALTER TABLE pacientes
    DROP CONSTRAINT IF EXISTS
        ck_pacientes_apellidos;

-- Restringe valores administrativos permitidos.
ALTER TABLE pacientes
    ADD CONSTRAINT ck_pacientes_tipo_documento
        CHECK (
            tipo_documento IN (
                'DNI',
                'CARNET_EXTRANJERIA',
                'PASAPORTE',
                'OTRO'
            )
        );

ALTER TABLE pacientes
    ADD CONSTRAINT ck_pacientes_estado
        CHECK (
            estado IN (
                'ACTIVO',
                'INACTIVO'
            )
        );

ALTER TABLE pacientes
    ADD CONSTRAINT ck_pacientes_numero_documento
        CHECK (
            LENGTH(TRIM(numero_documento))
                BETWEEN 4 AND 20
        );

ALTER TABLE pacientes
    ADD CONSTRAINT ck_pacientes_nombres
        CHECK (
            LENGTH(TRIM(nombres)) >= 2
        );

ALTER TABLE pacientes
    ADD CONSTRAINT ck_pacientes_apellidos
        CHECK (
            LENGTH(TRIM(apellidos)) >= 2
        );

-- Evita documentos duplicados dentro del mismo consultorio.
CREATE UNIQUE INDEX IF NOT EXISTS
    ux_pacientes_consultorio_documento
ON pacientes (
    id_consultorios,
    tipo_documento,
    numero_documento
);

-- Acelera el listado por consultorio y estado.
CREATE INDEX IF NOT EXISTS
    ix_pacientes_consultorio_estado
ON pacientes (
    id_consultorios,
    estado
);

-- Acelera la búsqueda alfabética de pacientes.
CREATE INDEX IF NOT EXISTS
    ix_pacientes_consultorio_nombres
ON pacientes (
    id_consultorios,
    apellidos,
    nombres
);

COMMENT ON TABLE pacientes IS
    'Datos administrativos de pacientes separados por consultorio.';

COMMENT ON COLUMN pacientes.id_pacientes IS
    'Clave primaria del paciente.';

COMMENT ON COLUMN pacientes.id_consultorios IS
    'Consultorio propietario de la información del paciente.';

COMMENT ON COLUMN pacientes.numero_documento IS
    'Documento único del paciente dentro del consultorio.';

COMMENT ON COLUMN pacientes.estado IS
    'Estado lógico del paciente sin eliminación física.';