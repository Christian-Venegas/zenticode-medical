-- Añade personalización para documentos clínicos y recetas.

-- Datos visuales del consultorio.
ALTER TABLE consultorios
    ADD COLUMN IF NOT EXISTS descripcion_documentos
        VARCHAR(200);

ALTER TABLE consultorios
    ADD COLUMN IF NOT EXISTS logo_url
        VARCHAR(500);

-- Datos profesionales del usuario.
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS especialidad
        VARCHAR(120);

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS firma_url
        VARCHAR(500);

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS sello_url
        VARCHAR(500);

-- Evita almacenar textos vacíos como configuración.
ALTER TABLE consultorios
    DROP CONSTRAINT IF EXISTS
        ck_consultorios_descripcion_documentos;

ALTER TABLE consultorios
    ADD CONSTRAINT ck_consultorios_descripcion_documentos
        CHECK (
            descripcion_documentos IS NULL
            OR LENGTH(
                TRIM(descripcion_documentos)
            ) BETWEEN 3 AND 200
        );

ALTER TABLE consultorios
    DROP CONSTRAINT IF EXISTS
        ck_consultorios_logo_url;

ALTER TABLE consultorios
    ADD CONSTRAINT ck_consultorios_logo_url
        CHECK (
            logo_url IS NULL
            OR LENGTH(
                TRIM(logo_url)
            ) BETWEEN 3 AND 500
        );

ALTER TABLE usuarios
    DROP CONSTRAINT IF EXISTS
        ck_usuarios_especialidad;

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_especialidad
        CHECK (
            especialidad IS NULL
            OR LENGTH(
                TRIM(especialidad)
            ) BETWEEN 3 AND 120
        );

ALTER TABLE usuarios
    DROP CONSTRAINT IF EXISTS
        ck_usuarios_firma_url;

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_firma_url
        CHECK (
            firma_url IS NULL
            OR LENGTH(
                TRIM(firma_url)
            ) BETWEEN 3 AND 500
        );

ALTER TABLE usuarios
    DROP CONSTRAINT IF EXISTS
        ck_usuarios_sello_url;

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_sello_url
        CHECK (
            sello_url IS NULL
            OR LENGTH(
                TRIM(sello_url)
            ) BETWEEN 3 AND 500
        );

COMMENT ON COLUMN consultorios.descripcion_documentos IS
    'Subtítulo mostrado en recetas y documentos clínicos.';

COMMENT ON COLUMN consultorios.logo_url IS
    'Ruta o URL controlada del logo del consultorio.';

COMMENT ON COLUMN usuarios.especialidad IS
    'Especialidad profesional mostrada en documentos clínicos.';

COMMENT ON COLUMN usuarios.firma_url IS
    'Ruta o URL controlada de la firma del profesional.';

COMMENT ON COLUMN usuarios.sello_url IS
    'Ruta o URL controlada del sello del profesional.';