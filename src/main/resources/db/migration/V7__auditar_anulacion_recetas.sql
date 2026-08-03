-- Añade auditoría completa para la anulación de recetas.

ALTER TABLE recetas
    ADD COLUMN IF NOT EXISTS fecha_anulacion
        TIMESTAMPTZ;

ALTER TABLE recetas
    ADD COLUMN IF NOT EXISTS anulado_por
        BIGINT;

-- Vincula el responsable de la anulación con usuarios.
ALTER TABLE recetas
    ADD CONSTRAINT fk_recetas_anulado_por
        FOREIGN KEY (
            anulado_por
        )
        REFERENCES usuarios (
            id_usuarios
        );

-- Reemplaza la restricción anterior por una auditoría completa.
ALTER TABLE recetas
    DROP CONSTRAINT IF EXISTS
        ck_recetas_anulacion;

ALTER TABLE recetas
    ADD CONSTRAINT ck_recetas_anulacion
        CHECK (
            (
                estado = 'EMITIDA'
                AND motivo_anulacion IS NULL
                AND fecha_anulacion IS NULL
                AND anulado_por IS NULL
            )
            OR
            (
                estado = 'ANULADA'
                AND motivo_anulacion IS NOT NULL
                AND LENGTH(
                    TRIM(motivo_anulacion)
                ) BETWEEN 5 AND 500
                AND fecha_anulacion IS NOT NULL
                AND anulado_por IS NOT NULL
            )
        );

-- Refuerza los únicos estados permitidos.
ALTER TABLE recetas
    DROP CONSTRAINT IF EXISTS
        ck_recetas_estado;

ALTER TABLE recetas
    ADD CONSTRAINT ck_recetas_estado
        CHECK (
            estado IN (
                'EMITIDA',
                'ANULADA'
            )
        );

-- Mejora búsquedas por consulta, paciente y estado.
CREATE INDEX IF NOT EXISTS
    ix_recetas_consultorio_consulta_estado
ON recetas (
    id_consultorios,
    id_consultas,
    estado,
    fecha_emision DESC
);

-- Mejora la carga ordenada de medicamentos.
CREATE INDEX IF NOT EXISTS
    ix_recetas_detalle_receta_orden
ON recetas_detalle (
    id_consultorios,
    id_recetas,
    orden
);

COMMENT ON COLUMN recetas.fecha_anulacion IS
    'Fecha exacta en que la receta fue anulada.';

COMMENT ON COLUMN recetas.anulado_por IS
    'Usuario responsable de anular la receta.';

COMMENT ON COLUMN recetas.motivo_anulacion IS
    'Justificación obligatoria registrada al anular la receta.';