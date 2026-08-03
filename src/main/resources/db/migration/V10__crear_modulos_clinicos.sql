-- =========================================================
-- ZENTICODE MEDICAL
-- Antecedentes, alergias y consultas clínicas
-- =========================================================

-- =========================================================
-- ANTECEDENTES CLÍNICOS
-- =========================================================

CREATE TABLE antecedentes_clinicos (
    id_antecedentes_clinicos BIGSERIAL
        CONSTRAINT pk_antecedentes_clinicos
        PRIMARY KEY,

    id_historias_clinicas BIGINT NOT NULL,

    tipo VARCHAR(30) NOT NULL,

    descripcion VARCHAR(1000) NOT NULL,

    fecha_aproximada DATE,

    observaciones VARCHAR(1000),

    importancia VARCHAR(20) NOT NULL
        DEFAULT 'MEDIA',

    activo BOOLEAN NOT NULL
        DEFAULT TRUE,

    creado_por BIGINT NOT NULL,

    modificado_por BIGINT NOT NULL,

    fecha_creacion TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_modificacion TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_antecedentes_clinicos_historia
        FOREIGN KEY (id_historias_clinicas)
        REFERENCES historias_clinicas (
            id_historias_clinicas
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT fk_antecedentes_clinicos_creado_por
        FOREIGN KEY (creado_por)
        REFERENCES usuarios (
            id_usuarios
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT fk_antecedentes_clinicos_modificado_por
        FOREIGN KEY (modificado_por)
        REFERENCES usuarios (
            id_usuarios
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT ck_antecedentes_clinicos_tipo
        CHECK (
            tipo IN (
                'PATOLOGICO',
                'QUIRURGICO',
                'FAMILIAR',
                'FARMACOLOGICO',
                'ALERGICO',
                'HABITO',
                'GINECO_OBSTETRICO',
                'OTRO'
            )
        ),

    CONSTRAINT ck_antecedentes_clinicos_importancia
        CHECK (
            importancia IN (
                'BAJA',
                'MEDIA',
                'ALTA',
                'CRITICA'
            )
        ),

    CONSTRAINT ck_antecedentes_clinicos_descripcion
        CHECK (
            LENGTH(TRIM(descripcion))
            BETWEEN 1 AND 1000
        ),

    CONSTRAINT ck_antecedentes_clinicos_observaciones
        CHECK (
            observaciones IS NULL
            OR LENGTH(TRIM(observaciones))
                BETWEEN 1 AND 1000
        )
);

CREATE INDEX idx_antecedentes_historia_activo
    ON antecedentes_clinicos (
        id_historias_clinicas,
        activo
    );

CREATE INDEX idx_antecedentes_historia_tipo
    ON antecedentes_clinicos (
        id_historias_clinicas,
        tipo
    );

COMMENT ON TABLE antecedentes_clinicos IS
    'Antecedentes clínicos acumulados de una historia.';

-- =========================================================
-- ALERGIAS CLÍNICAS
-- =========================================================

CREATE TABLE alergias_clinicas (
    id_alergias_clinicas BIGSERIAL
        CONSTRAINT pk_alergias_clinicas
        PRIMARY KEY,

    id_historias_clinicas BIGINT NOT NULL,

    tipo VARCHAR(20) NOT NULL,

    sustancia VARCHAR(200) NOT NULL,

    reaccion VARCHAR(500),

    gravedad VARCHAR(20) NOT NULL
        DEFAULT 'MEDIA',

    estado VARCHAR(20) NOT NULL
        DEFAULT 'ACTIVA',

    observaciones VARCHAR(1000),

    creado_por BIGINT NOT NULL,

    modificado_por BIGINT NOT NULL,

    fecha_creacion TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_modificacion TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_alergias_clinicas_historia
        FOREIGN KEY (id_historias_clinicas)
        REFERENCES historias_clinicas (
            id_historias_clinicas
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT fk_alergias_clinicas_creado_por
        FOREIGN KEY (creado_por)
        REFERENCES usuarios (
            id_usuarios
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT fk_alergias_clinicas_modificado_por
        FOREIGN KEY (modificado_por)
        REFERENCES usuarios (
            id_usuarios
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT ck_alergias_clinicas_tipo
        CHECK (
            tipo IN (
                'MEDICAMENTO',
                'ALIMENTO',
                'AMBIENTAL',
                'MATERIAL',
                'OTRA'
            )
        ),

    CONSTRAINT ck_alergias_clinicas_gravedad
        CHECK (
            gravedad IN (
                'BAJA',
                'MEDIA',
                'ALTA',
                'CRITICA'
            )
        ),

    CONSTRAINT ck_alergias_clinicas_estado
        CHECK (
            estado IN (
                'ACTIVA',
                'INACTIVA',
                'DESCARTADA'
            )
        ),

    CONSTRAINT ck_alergias_clinicas_sustancia
        CHECK (
            LENGTH(TRIM(sustancia))
            BETWEEN 1 AND 200
        ),

    CONSTRAINT ck_alergias_clinicas_reaccion
        CHECK (
            reaccion IS NULL
            OR LENGTH(TRIM(reaccion))
                BETWEEN 1 AND 500
        ),

    CONSTRAINT ck_alergias_clinicas_observaciones
        CHECK (
            observaciones IS NULL
            OR LENGTH(TRIM(observaciones))
                BETWEEN 1 AND 1000
        )
);

CREATE INDEX idx_alergias_historia_estado
    ON alergias_clinicas (
        id_historias_clinicas,
        estado
    );

CREATE INDEX idx_alergias_historia_gravedad
    ON alergias_clinicas (
        id_historias_clinicas,
        gravedad
    );

COMMENT ON TABLE alergias_clinicas IS
    'Alergias documentadas dentro de una historia clínica.';

-- =========================================================
-- CONSULTAS CLÍNICAS
-- =========================================================

CREATE TABLE consultas_clinicas (
    id_consultas_clinicas BIGSERIAL
        CONSTRAINT pk_consultas_clinicas
        PRIMARY KEY,

    id_historias_clinicas BIGINT NOT NULL,

    id_consultorios BIGINT NOT NULL,

    id_pacientes BIGINT NOT NULL,

    id_usuarios_profesional BIGINT NOT NULL,

    fecha_consulta TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    motivo_consulta VARCHAR(1000) NOT NULL,

    enfermedad_actual VARCHAR(3000),

    diagnostico_principal VARCHAR(1000),

    plan_tratamiento VARCHAR(3000),

    observaciones VARCHAR(2000),

    estado VARCHAR(20) NOT NULL
        DEFAULT 'BORRADOR',

    fecha_creacion TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_modificacion TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_consultas_clinicas_historia
        FOREIGN KEY (id_historias_clinicas)
        REFERENCES historias_clinicas (
            id_historias_clinicas
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT fk_consultas_clinicas_consultorio
        FOREIGN KEY (id_consultorios)
        REFERENCES consultorios (
            id_consultorios
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT fk_consultas_clinicas_paciente
        FOREIGN KEY (id_pacientes)
        REFERENCES pacientes (
            id_pacientes
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT fk_consultas_clinicas_profesional
        FOREIGN KEY (id_usuarios_profesional)
        REFERENCES usuarios (
            id_usuarios
        )
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,

    CONSTRAINT ck_consultas_clinicas_motivo
        CHECK (
            LENGTH(TRIM(motivo_consulta))
            BETWEEN 1 AND 1000
        ),

    CONSTRAINT ck_consultas_clinicas_enfermedad_actual
        CHECK (
            enfermedad_actual IS NULL
            OR LENGTH(TRIM(enfermedad_actual))
                BETWEEN 1 AND 3000
        ),

    CONSTRAINT ck_consultas_clinicas_diagnostico
        CHECK (
            diagnostico_principal IS NULL
            OR LENGTH(TRIM(diagnostico_principal))
                BETWEEN 1 AND 1000
        ),

    CONSTRAINT ck_consultas_clinicas_plan
        CHECK (
            plan_tratamiento IS NULL
            OR LENGTH(TRIM(plan_tratamiento))
                BETWEEN 1 AND 3000
        ),

    CONSTRAINT ck_consultas_clinicas_observaciones
        CHECK (
            observaciones IS NULL
            OR LENGTH(TRIM(observaciones))
                BETWEEN 1 AND 2000
        ),

    CONSTRAINT ck_consultas_clinicas_estado
        CHECK (
            estado IN (
                'BORRADOR',
                'FINALIZADA',
                'ANULADA'
            )
        )
);

CREATE INDEX idx_consultas_clinicas_historia_fecha
    ON consultas_clinicas (
        id_historias_clinicas,
        fecha_consulta DESC
    );

CREATE INDEX idx_consultas_clinicas_consultorio_fecha
    ON consultas_clinicas (
        id_consultorios,
        fecha_consulta DESC
    );

CREATE INDEX idx_consultas_clinicas_paciente_fecha
    ON consultas_clinicas (
        id_pacientes,
        fecha_consulta DESC
    );

CREATE INDEX idx_consultas_clinicas_profesional
    ON consultas_clinicas (
        id_usuarios_profesional,
        fecha_consulta DESC
    );

COMMENT ON TABLE consultas_clinicas IS
    'Atenciones médicas realizadas dentro de una historia clínica.';

COMMENT ON COLUMN consultas_clinicas.estado IS
    'Estado BORRADOR, FINALIZADA o ANULADA.';