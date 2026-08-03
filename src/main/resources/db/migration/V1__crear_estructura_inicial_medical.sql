-- ============================================================================
-- ZENTICODE MEDICAL - MIGRACION INICIAL POSTGRESQL
-- Archivo: V1__crear_estructura_inicial_medical.sql
-- Objetivo: nucleo clinico y administrativo para medicina general.
-- Estrategia: monolito modular, multi-consultorio preparado desde el esquema.
-- Convencion: toda PK/FK usa nombre completo; nunca se usa solamente "id".
-- IMPORTANTE: ejecutar con Flyway sobre una base vacia.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Funciones comunes
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_actualizar_fecha_modificacion()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.fecha_modificacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

-- ----------------------------------------------------------------------------
-- 2. Consultorios, usuarios y autorizacion
-- ----------------------------------------------------------------------------
CREATE TABLE consultorios (
    id_consultorios              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_publico               VARCHAR(36) NOT NULL UNIQUE,
    nombre                       VARCHAR(150) NOT NULL,
    ruc                          VARCHAR(11),
    telefono                     VARCHAR(20),
    correo                       VARCHAR(180),
    direccion                    VARCHAR(250),
    zona_horaria                 VARCHAR(60) NOT NULL DEFAULT 'America/Lima',
    moneda                       CHAR(3) NOT NULL DEFAULT 'PEN',
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_consultorios_nombre CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_consultorios_ruc CHECK (ruc IS NULL OR ruc ~ '^[0-9]{11}$'),
    CONSTRAINT ck_consultorios_moneda CHECK (moneda ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_consultorios_estado CHECK (estado IN ('ACTIVO','SUSPENDIDO','INACTIVO'))
);

CREATE TABLE usuarios (
    id_usuarios                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    correo                       VARCHAR(180) NOT NULL,
    password_hash                VARCHAR(255) NOT NULL,
    nombres                      VARCHAR(120) NOT NULL,
    apellidos                    VARCHAR(120) NOT NULL,
    numero_colegiatura           VARCHAR(40),
    telefono                     VARCHAR(20),
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    intentos_fallidos            SMALLINT NOT NULL DEFAULT 0,
    bloqueado_hasta              TIMESTAMPTZ,
    ultimo_acceso                TIMESTAMPTZ,
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_consultorios
        FOREIGN KEY (id_consultorios) REFERENCES consultorios(id_consultorios),
    CONSTRAINT uq_usuarios_tenant_correo UNIQUE (id_consultorios, correo),
    CONSTRAINT uq_usuarios_tenant_id UNIQUE (id_consultorios, id_usuarios),
    CONSTRAINT ck_usuarios_correo CHECK (correo = lower(btrim(correo)) AND position('@' IN correo) > 1),
    CONSTRAINT ck_usuarios_hash CHECK (length(password_hash) >= 20),
    CONSTRAINT ck_usuarios_nombres CHECK (btrim(nombres) <> '' AND btrim(apellidos) <> ''),
    CONSTRAINT ck_usuarios_estado CHECK (estado IN ('ACTIVO','BLOQUEADO','INACTIVO')),
    CONSTRAINT ck_usuarios_intentos CHECK (intentos_fallidos >= 0)
);

CREATE TABLE roles (
    id_roles                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo                       VARCHAR(40) NOT NULL UNIQUE,
    nombre                       VARCHAR(80) NOT NULL,
    descripcion                  VARCHAR(250),
    es_sistema                   BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_roles_codigo CHECK (codigo ~ '^[A-Z][A-Z0-9_]*$'),
    CONSTRAINT ck_roles_nombre CHECK (btrim(nombre) <> '')
);

CREATE TABLE usuarios_roles (
    id_usuarios_roles            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_usuarios                  BIGINT NOT NULL,
    id_roles                     BIGINT NOT NULL,
    fecha_asignacion             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asignado_por                 BIGINT,
    CONSTRAINT fk_usuarios_roles_usuario_tenant
        FOREIGN KEY (id_consultorios, id_usuarios)
        REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT fk_usuarios_roles_roles
        FOREIGN KEY (id_roles) REFERENCES roles(id_roles),
    CONSTRAINT fk_usuarios_roles_asignador_tenant
        FOREIGN KEY (id_consultorios, asignado_por)
        REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_usuarios_roles UNIQUE (id_consultorios, id_usuarios, id_roles)
);

INSERT INTO roles (codigo, nombre, descripcion) VALUES
('MEDICO', 'Medico', 'Acceso clinico y administrativo del profesional'),
('ASISTENTE', 'Asistente', 'Acceso administrativo limitado'),
('ADMIN_CONSULTORIO', 'Administrador del consultorio', 'Configuracion del consultorio');

-- ----------------------------------------------------------------------------
-- 3. Pacientes e historia longitudinal
-- ----------------------------------------------------------------------------
CREATE TABLE pacientes (
    id_pacientes                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    tipo_documento               VARCHAR(20),
    numero_documento             VARCHAR(30),
    nombres                      VARCHAR(120) NOT NULL,
    apellido_paterno             VARCHAR(80) NOT NULL,
    apellido_materno             VARCHAR(80),
    fecha_nacimiento             DATE,
    sexo_registrado              VARCHAR(30),
    telefono                     VARCHAR(20),
    correo                       VARCHAR(180),
    direccion                    VARCHAR(250),
    contacto_emergencia_nombre   VARCHAR(160),
    contacto_emergencia_telefono VARCHAR(20),
    observaciones_administrativas VARCHAR(500),
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por                   BIGINT NOT NULL,
    modificado_por               BIGINT NOT NULL,
    CONSTRAINT fk_pacientes_consultorios
        FOREIGN KEY (id_consultorios) REFERENCES consultorios(id_consultorios),
    CONSTRAINT fk_pacientes_creado_tenant
        FOREIGN KEY (id_consultorios, creado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT fk_pacientes_modificado_tenant
        FOREIGN KEY (id_consultorios, modificado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_pacientes_tenant_id UNIQUE (id_consultorios, id_pacientes),
    CONSTRAINT ck_pacientes_nombres CHECK (btrim(nombres) <> '' AND btrim(apellido_paterno) <> ''),
    CONSTRAINT ck_pacientes_documento CHECK (
        (tipo_documento IS NULL AND numero_documento IS NULL) OR
        (tipo_documento IS NOT NULL AND numero_documento IS NOT NULL AND btrim(numero_documento) <> '')
    ),
    CONSTRAINT ck_pacientes_nacimiento CHECK (fecha_nacimiento IS NULL OR fecha_nacimiento >= DATE '1900-01-01'),
    -- La regla 'no futura' se valida tambien en API porque CURRENT_DATE no debe fijarse en un CHECK persistente.
    CONSTRAINT ck_pacientes_estado CHECK (estado IN ('ACTIVO','INACTIVO','FALLECIDO'))
);

CREATE UNIQUE INDEX uq_pacientes_documento_activo
    ON pacientes (id_consultorios, tipo_documento, numero_documento)
    WHERE numero_documento IS NOT NULL AND estado <> 'INACTIVO';
CREATE INDEX ix_pacientes_busqueda_nombre
    ON pacientes (id_consultorios, apellido_paterno, nombres);
CREATE INDEX ix_pacientes_telefono
    ON pacientes (id_consultorios, telefono) WHERE telefono IS NOT NULL;

CREATE TABLE historias_clinicas (
    id_historias_clinicas        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pacientes                 BIGINT NOT NULL,
    grupo_sanguineo              VARCHAR(5),
    antecedentes_personales      TEXT,
    antecedentes_familiares      TEXT,
    antecedentes_quirurgicos     TEXT,
    antecedentes_farmacologicos  TEXT,
    observaciones_generales      TEXT,
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_apertura               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por                   BIGINT NOT NULL,
    modificado_por               BIGINT NOT NULL,
    CONSTRAINT fk_historias_pacientes_tenant
        FOREIGN KEY (id_consultorios, id_pacientes)
        REFERENCES pacientes(id_consultorios, id_pacientes),
    CONSTRAINT fk_historias_creado_tenant
        FOREIGN KEY (id_consultorios, creado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT fk_historias_modificado_tenant
        FOREIGN KEY (id_consultorios, modificado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_historias_tenant_paciente UNIQUE (id_consultorios, id_pacientes),
    CONSTRAINT uq_historias_tenant_id UNIQUE (id_consultorios, id_historias_clinicas),
    CONSTRAINT ck_historias_grupo CHECK (grupo_sanguineo IS NULL OR grupo_sanguineo IN ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
    CONSTRAINT ck_historias_estado CHECK (estado IN ('ACTIVA','CERRADA','ANULADA'))
);

CREATE TABLE alergias_pacientes (
    id_alergias_pacientes        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pacientes                 BIGINT NOT NULL,
    sustancia                    VARCHAR(180) NOT NULL,
    reaccion                     VARCHAR(500),
    severidad                    VARCHAR(20) NOT NULL DEFAULT 'NO_ESPECIFICADA',
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_registro               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registrado_por               BIGINT NOT NULL,
    CONSTRAINT fk_alergias_pacientes_tenant
        FOREIGN KEY (id_consultorios, id_pacientes) REFERENCES pacientes(id_consultorios, id_pacientes),
    CONSTRAINT fk_alergias_usuario_tenant
        FOREIGN KEY (id_consultorios, registrado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_alergias_tenant_id UNIQUE (id_consultorios, id_alergias_pacientes),
    CONSTRAINT ck_alergias_sustancia CHECK (btrim(sustancia) <> ''),
    CONSTRAINT ck_alergias_severidad CHECK (severidad IN ('LEVE','MODERADA','GRAVE','NO_ESPECIFICADA')),
    CONSTRAINT ck_alergias_estado CHECK (estado IN ('ACTIVA','INACTIVA','ERRONEA'))
);
CREATE INDEX ix_alergias_paciente ON alergias_pacientes(id_consultorios, id_pacientes, estado);

CREATE TABLE medicaciones_actuales (
    id_medicaciones_actuales     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pacientes                 BIGINT NOT NULL,
    medicamento                  VARCHAR(180) NOT NULL,
    dosis                        VARCHAR(100),
    frecuencia                   VARCHAR(100),
    fecha_inicio                 DATE,
    fecha_fin                    DATE,
    indicacion                   VARCHAR(500),
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_registro               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registrado_por               BIGINT NOT NULL,
    CONSTRAINT fk_medicaciones_paciente_tenant
        FOREIGN KEY (id_consultorios, id_pacientes) REFERENCES pacientes(id_consultorios, id_pacientes),
    CONSTRAINT fk_medicaciones_usuario_tenant
        FOREIGN KEY (id_consultorios, registrado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT ck_medicaciones_nombre CHECK (btrim(medicamento) <> ''),
    CONSTRAINT ck_medicaciones_fechas CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT ck_medicaciones_estado CHECK (estado IN ('ACTIVA','FINALIZADA','SUSPENDIDA','ERRONEA'))
);
CREATE INDEX ix_medicaciones_paciente ON medicaciones_actuales(id_consultorios, id_pacientes, estado);

-- ----------------------------------------------------------------------------
-- 4. Agenda, consultas, signos, diagnosticos y evoluciones
-- ----------------------------------------------------------------------------
CREATE TABLE citas (
    id_citas                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pacientes                 BIGINT NOT NULL,
    id_usuarios_medico           BIGINT NOT NULL,
    fecha_hora_inicio            TIMESTAMPTZ NOT NULL,
    fecha_hora_fin               TIMESTAMPTZ NOT NULL,
    motivo                       VARCHAR(500),
    estado                       VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    observaciones                VARCHAR(500),
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por                   BIGINT NOT NULL,
    CONSTRAINT fk_citas_paciente_tenant
        FOREIGN KEY (id_consultorios, id_pacientes) REFERENCES pacientes(id_consultorios, id_pacientes),
    CONSTRAINT fk_citas_medico_tenant
        FOREIGN KEY (id_consultorios, id_usuarios_medico) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT fk_citas_creado_tenant
        FOREIGN KEY (id_consultorios, creado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_citas_tenant_id UNIQUE (id_consultorios, id_citas),
    CONSTRAINT ck_citas_rango CHECK (fecha_hora_fin > fecha_hora_inicio),
    CONSTRAINT ck_citas_estado CHECK (estado IN ('PROGRAMADA','CONFIRMADA','ATENDIDA','CANCELADA','NO_ASISTIO'))
);
CREATE INDEX ix_citas_agenda ON citas(id_consultorios, id_usuarios_medico, fecha_hora_inicio);
CREATE INDEX ix_citas_paciente ON citas(id_consultorios, id_pacientes, fecha_hora_inicio DESC);

CREATE TABLE consultas (
    id_consultas                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pacientes                 BIGINT NOT NULL,
    id_historias_clinicas        BIGINT NOT NULL,
    id_usuarios_medico           BIGINT NOT NULL,
    id_citas                     BIGINT,
    fecha_hora_atencion          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo_consulta              TEXT NOT NULL,
    anamnesis                    TEXT,
    examen_fisico                TEXT,
    evaluacion_clinica           TEXT,
    plan_tratamiento             TEXT,
    recomendaciones              TEXT,
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    motivo_anulacion             VARCHAR(500),
    fecha_anulacion              TIMESTAMPTZ,
    anulado_por                  BIGINT,
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por                   BIGINT NOT NULL,
    modificado_por               BIGINT NOT NULL,
    CONSTRAINT fk_consultas_paciente_tenant
        FOREIGN KEY (id_consultorios, id_pacientes) REFERENCES pacientes(id_consultorios, id_pacientes),
    CONSTRAINT fk_consultas_historia_tenant
        FOREIGN KEY (id_consultorios, id_historias_clinicas) REFERENCES historias_clinicas(id_consultorios, id_historias_clinicas),
    CONSTRAINT fk_consultas_medico_tenant
        FOREIGN KEY (id_consultorios, id_usuarios_medico) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT fk_consultas_cita_tenant
        FOREIGN KEY (id_consultorios, id_citas) REFERENCES citas(id_consultorios, id_citas),
    CONSTRAINT fk_consultas_creado_tenant
        FOREIGN KEY (id_consultorios, creado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT fk_consultas_modificado_tenant
        FOREIGN KEY (id_consultorios, modificado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT fk_consultas_anulado_tenant
        FOREIGN KEY (id_consultorios, anulado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_consultas_tenant_id UNIQUE (id_consultorios, id_consultas),
    CONSTRAINT uq_consultas_cita UNIQUE (id_consultorios, id_citas),
    CONSTRAINT ck_consultas_motivo CHECK (btrim(motivo_consulta) <> ''),
    CONSTRAINT ck_consultas_estado CHECK (estado IN ('ABIERTA','CERRADA','ANULADA')),
    CONSTRAINT ck_consultas_anulacion CHECK (
        (estado <> 'ANULADA' AND motivo_anulacion IS NULL AND fecha_anulacion IS NULL AND anulado_por IS NULL)
        OR
        (estado = 'ANULADA' AND motivo_anulacion IS NOT NULL AND fecha_anulacion IS NOT NULL AND anulado_por IS NOT NULL)
    )
);
CREATE INDEX ix_consultas_paciente_fecha ON consultas(id_consultorios, id_pacientes, fecha_hora_atencion DESC);
CREATE INDEX ix_consultas_reporte_mes ON consultas(id_consultorios, fecha_hora_atencion, estado);

CREATE TABLE signos_vitales (
    id_signos_vitales            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_consultas                 BIGINT NOT NULL,
    temperatura_c                NUMERIC(4,1),
    presion_sistolica_mmhg       SMALLINT,
    presion_diastolica_mmhg      SMALLINT,
    frecuencia_cardiaca_lpm      SMALLINT,
    frecuencia_respiratoria_rpm  SMALLINT,
    saturacion_oxigeno_pct       NUMERIC(5,2),
    peso_kg                      NUMERIC(6,2),
    talla_cm                     NUMERIC(6,2),
    perimetro_abdominal_cm       NUMERIC(6,2),
    observaciones                VARCHAR(500),
    fecha_registro               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registrado_por               BIGINT NOT NULL,
    CONSTRAINT fk_signos_consulta_tenant
        FOREIGN KEY (id_consultorios, id_consultas) REFERENCES consultas(id_consultorios, id_consultas),
    CONSTRAINT fk_signos_usuario_tenant
        FOREIGN KEY (id_consultorios, registrado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_signos_consulta UNIQUE (id_consultorios, id_consultas),
    CONSTRAINT ck_signos_temperatura CHECK (temperatura_c IS NULL OR temperatura_c BETWEEN 25 AND 50),
    CONSTRAINT ck_signos_presion CHECK (
        (presion_sistolica_mmhg IS NULL OR presion_sistolica_mmhg BETWEEN 30 AND 300) AND
        (presion_diastolica_mmhg IS NULL OR presion_diastolica_mmhg BETWEEN 20 AND 200)
    ),
    CONSTRAINT ck_signos_frecuencias CHECK (
        (frecuencia_cardiaca_lpm IS NULL OR frecuencia_cardiaca_lpm BETWEEN 1 AND 300) AND
        (frecuencia_respiratoria_rpm IS NULL OR frecuencia_respiratoria_rpm BETWEEN 1 AND 100)
    ),
    CONSTRAINT ck_signos_saturacion CHECK (saturacion_oxigeno_pct IS NULL OR saturacion_oxigeno_pct BETWEEN 0 AND 100),
    CONSTRAINT ck_signos_medidas CHECK (
        (peso_kg IS NULL OR peso_kg > 0) AND
        (talla_cm IS NULL OR talla_cm > 0) AND
        (perimetro_abdominal_cm IS NULL OR perimetro_abdominal_cm > 0)
    )
);

CREATE TABLE diagnosticos_consultas (
    id_diagnosticos_consultas    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_consultas                 BIGINT NOT NULL,
    codigo_cie10                 VARCHAR(12),
    descripcion                  VARCHAR(500) NOT NULL,
    tipo                         VARCHAR(20) NOT NULL DEFAULT 'PRESUNTIVO',
    principal                    BOOLEAN NOT NULL DEFAULT FALSE,
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_registro               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registrado_por               BIGINT NOT NULL,
    CONSTRAINT fk_diagnosticos_consulta_tenant
        FOREIGN KEY (id_consultorios, id_consultas) REFERENCES consultas(id_consultorios, id_consultas),
    CONSTRAINT fk_diagnosticos_usuario_tenant
        FOREIGN KEY (id_consultorios, registrado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT ck_diagnosticos_descripcion CHECK (btrim(descripcion) <> ''),
    CONSTRAINT ck_diagnosticos_tipo CHECK (tipo IN ('PRESUNTIVO','DEFINITIVO','REPETITIVO')),
    CONSTRAINT ck_diagnosticos_estado CHECK (estado IN ('ACTIVO','DESCARTADO','ERRONEO'))
);
CREATE UNIQUE INDEX uq_diagnostico_principal_consulta
    ON diagnosticos_consultas(id_consultorios, id_consultas)
    WHERE principal = TRUE AND estado = 'ACTIVO';

CREATE TABLE evoluciones (
    id_evoluciones               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_consultas                 BIGINT NOT NULL,
    nota_evolucion               TEXT NOT NULL,
    fecha_evolucion              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registrado_por               BIGINT NOT NULL,
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    motivo_anulacion             VARCHAR(500),
    CONSTRAINT fk_evoluciones_consulta_tenant
        FOREIGN KEY (id_consultorios, id_consultas) REFERENCES consultas(id_consultorios, id_consultas),
    CONSTRAINT fk_evoluciones_usuario_tenant
        FOREIGN KEY (id_consultorios, registrado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT ck_evoluciones_nota CHECK (btrim(nota_evolucion) <> ''),
    CONSTRAINT ck_evoluciones_estado CHECK (estado IN ('ACTIVA','ANULADA')),
    CONSTRAINT ck_evoluciones_anulacion CHECK (
        (estado = 'ACTIVA' AND motivo_anulacion IS NULL) OR
        (estado = 'ANULADA' AND motivo_anulacion IS NOT NULL)
    )
);
CREATE INDEX ix_evoluciones_consulta ON evoluciones(id_consultorios, id_consultas, fecha_evolucion DESC);

-- ----------------------------------------------------------------------------
-- 5. Recetas
-- ----------------------------------------------------------------------------
CREATE TABLE recetas (
    id_recetas                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_consultas                 BIGINT NOT NULL,
    fecha_emision                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    indicaciones_generales       TEXT,
    estado                       VARCHAR(20) NOT NULL DEFAULT 'EMITIDA',
    motivo_anulacion             VARCHAR(500),
    emitido_por                  BIGINT NOT NULL,
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recetas_consulta_tenant
        FOREIGN KEY (id_consultorios, id_consultas) REFERENCES consultas(id_consultorios, id_consultas),
    CONSTRAINT fk_recetas_usuario_tenant
        FOREIGN KEY (id_consultorios, emitido_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_recetas_tenant_id UNIQUE (id_consultorios, id_recetas),
    CONSTRAINT ck_recetas_estado CHECK (estado IN ('BORRADOR','EMITIDA','ANULADA')),
    CONSTRAINT ck_recetas_anulacion CHECK (
        (estado <> 'ANULADA' AND motivo_anulacion IS NULL) OR
        (estado = 'ANULADA' AND motivo_anulacion IS NOT NULL)
    )
);
CREATE INDEX ix_recetas_consulta ON recetas(id_consultorios, id_consultas, fecha_emision DESC);

CREATE TABLE recetas_detalle (
    id_recetas_detalle           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_recetas                   BIGINT NOT NULL,
    medicamento                  VARCHAR(200) NOT NULL,
    presentacion                 VARCHAR(120),
    dosis                        VARCHAR(120) NOT NULL,
    via_administracion           VARCHAR(80),
    frecuencia                   VARCHAR(120) NOT NULL,
    duracion                     VARCHAR(120) NOT NULL,
    indicaciones                 VARCHAR(500),
    orden                        SMALLINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_recetas_detalle_receta_tenant
        FOREIGN KEY (id_consultorios, id_recetas) REFERENCES recetas(id_consultorios, id_recetas),
    CONSTRAINT ck_recetas_detalle_campos CHECK (
        btrim(medicamento) <> '' AND btrim(dosis) <> '' AND btrim(frecuencia) <> '' AND btrim(duracion) <> ''
    ),
    CONSTRAINT ck_recetas_detalle_orden CHECK (orden > 0),
    CONSTRAINT uq_recetas_detalle_orden UNIQUE (id_consultorios, id_recetas, orden)
);

-- ----------------------------------------------------------------------------
-- 6. Examenes, ecografias y archivos clinicos
-- ----------------------------------------------------------------------------
CREATE TABLE tipos_examenes (
    id_tipos_examenes            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    categoria                    VARCHAR(20) NOT NULL,
    nombre                       VARCHAR(180) NOT NULL,
    descripcion                  VARCHAR(500),
    precio_referencial           NUMERIC(12,2),
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tipos_examenes_consultorio
        FOREIGN KEY (id_consultorios) REFERENCES consultorios(id_consultorios),
    CONSTRAINT uq_tipos_examenes_tenant_id UNIQUE (id_consultorios, id_tipos_examenes),
    CONSTRAINT uq_tipos_examenes_nombre UNIQUE (id_consultorios, categoria, nombre),
    CONSTRAINT ck_tipos_examenes_categoria CHECK (categoria IN ('LABORATORIO','ECOGRAFIA','IMAGEN','OTRO')),
    CONSTRAINT ck_tipos_examenes_nombre CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_tipos_examenes_precio CHECK (precio_referencial IS NULL OR precio_referencial >= 0),
    CONSTRAINT ck_tipos_examenes_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE examenes (
    id_examenes                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pacientes                 BIGINT NOT NULL,
    id_consultas                 BIGINT,
    id_tipos_examenes            BIGINT NOT NULL,
    fecha_solicitud              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_realizacion            TIMESTAMPTZ,
    fecha_resultado              TIMESTAMPTZ,
    estado                       VARCHAR(20) NOT NULL DEFAULT 'SOLICITADO',
    resultado_texto              TEXT,
    conclusiones                 TEXT,
    observaciones                TEXT,
    solicitado_por               BIGINT NOT NULL,
    revisado_por                 BIGINT,
    fecha_revision               TIMESTAMPTZ,
    motivo_anulacion             VARCHAR(500),
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_examenes_paciente_tenant
        FOREIGN KEY (id_consultorios, id_pacientes) REFERENCES pacientes(id_consultorios, id_pacientes),
    CONSTRAINT fk_examenes_consulta_tenant
        FOREIGN KEY (id_consultorios, id_consultas) REFERENCES consultas(id_consultorios, id_consultas),
    CONSTRAINT fk_examenes_tipo_tenant
        FOREIGN KEY (id_consultorios, id_tipos_examenes) REFERENCES tipos_examenes(id_consultorios, id_tipos_examenes),
    CONSTRAINT fk_examenes_solicitado_tenant
        FOREIGN KEY (id_consultorios, solicitado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT fk_examenes_revisado_tenant
        FOREIGN KEY (id_consultorios, revisado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_examenes_tenant_id UNIQUE (id_consultorios, id_examenes),
    CONSTRAINT ck_examenes_estado CHECK (estado IN ('SOLICITADO','REALIZADO','RESULTADO_RECIBIDO','REVISADO','ANULADO')),
    CONSTRAINT ck_examenes_fechas CHECK (
        (fecha_realizacion IS NULL OR fecha_realizacion >= fecha_solicitud) AND
        (fecha_resultado IS NULL OR fecha_realizacion IS NULL OR fecha_resultado >= fecha_realizacion) AND
        (fecha_revision IS NULL OR fecha_resultado IS NULL OR fecha_revision >= fecha_resultado)
    ),
    CONSTRAINT ck_examenes_revision CHECK (
        (estado <> 'REVISADO') OR (revisado_por IS NOT NULL AND fecha_revision IS NOT NULL)
    ),
    CONSTRAINT ck_examenes_anulacion CHECK (
        (estado <> 'ANULADO' AND motivo_anulacion IS NULL) OR
        (estado = 'ANULADO' AND motivo_anulacion IS NOT NULL)
    )
);
CREATE INDEX ix_examenes_paciente_fecha ON examenes(id_consultorios, id_pacientes, fecha_solicitud DESC);
CREATE INDEX ix_examenes_reporte_mes ON examenes(id_consultorios, id_tipos_examenes, fecha_realizacion, estado);

CREATE TABLE archivos_clinicos (
    id_archivos_clinicos         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pacientes                 BIGINT NOT NULL,
    id_consultas                 BIGINT,
    id_examenes                  BIGINT,
    nombre_original              VARCHAR(255) NOT NULL,
    nombre_almacenado            VARCHAR(255) NOT NULL,
    tipo_mime                    VARCHAR(120) NOT NULL,
    tamano_bytes                 BIGINT NOT NULL,
    ubicacion_privada            VARCHAR(500) NOT NULL,
    hash_sha256                  CHAR(64) NOT NULL,
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_carga                  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cargado_por                  BIGINT NOT NULL,
    motivo_anulacion             VARCHAR(500),
    CONSTRAINT fk_archivos_paciente_tenant
        FOREIGN KEY (id_consultorios, id_pacientes) REFERENCES pacientes(id_consultorios, id_pacientes),
    CONSTRAINT fk_archivos_consulta_tenant
        FOREIGN KEY (id_consultorios, id_consultas) REFERENCES consultas(id_consultorios, id_consultas),
    CONSTRAINT fk_archivos_examen_tenant
        FOREIGN KEY (id_consultorios, id_examenes) REFERENCES examenes(id_consultorios, id_examenes),
    CONSTRAINT fk_archivos_usuario_tenant
        FOREIGN KEY (id_consultorios, cargado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT ck_archivos_nombre CHECK (btrim(nombre_original) <> '' AND btrim(nombre_almacenado) <> ''),
    CONSTRAINT ck_archivos_tamano CHECK (tamano_bytes > 0),
    CONSTRAINT ck_archivos_hash CHECK (hash_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_archivos_mime CHECK (tipo_mime IN ('application/pdf','image/jpeg','image/png','image/webp')),
    CONSTRAINT ck_archivos_vinculo CHECK (id_consultas IS NOT NULL OR id_examenes IS NOT NULL),
    CONSTRAINT ck_archivos_estado CHECK (estado IN ('ACTIVO','ANULADO')),
    CONSTRAINT ck_archivos_anulacion CHECK (
        (estado = 'ACTIVO' AND motivo_anulacion IS NULL) OR
        (estado = 'ANULADO' AND motivo_anulacion IS NOT NULL)
    )
);
CREATE INDEX ix_archivos_paciente ON archivos_clinicos(id_consultorios, id_pacientes, fecha_carga DESC);
CREATE INDEX ix_archivos_examen ON archivos_clinicos(id_consultorios, id_examenes) WHERE id_examenes IS NOT NULL;

-- ----------------------------------------------------------------------------
-- 7. Servicios, cargos y pagos manuales
-- ----------------------------------------------------------------------------
CREATE TABLE servicios (
    id_servicios                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    codigo                       VARCHAR(40) NOT NULL,
    nombre                       VARCHAR(180) NOT NULL,
    categoria                    VARCHAR(30) NOT NULL,
    precio_base                  NUMERIC(12,2) NOT NULL DEFAULT 0,
    estado                       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_servicios_consultorio
        FOREIGN KEY (id_consultorios) REFERENCES consultorios(id_consultorios),
    CONSTRAINT uq_servicios_tenant_id UNIQUE (id_consultorios, id_servicios),
    CONSTRAINT uq_servicios_codigo UNIQUE (id_consultorios, codigo),
    CONSTRAINT ck_servicios_codigo CHECK (codigo ~ '^[A-Z0-9_-]+$'),
    CONSTRAINT ck_servicios_nombre CHECK (btrim(nombre) <> ''),
    CONSTRAINT ck_servicios_categoria CHECK (categoria IN ('CONSULTA','LABORATORIO','ECOGRAFIA','PROCEDIMIENTO','OTRO')),
    CONSTRAINT ck_servicios_precio CHECK (precio_base >= 0),
    CONSTRAINT ck_servicios_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE cargos_servicios (
    id_cargos_servicios          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pacientes                 BIGINT NOT NULL,
    id_consultas                 BIGINT,
    id_examenes                  BIGINT,
    id_servicios                 BIGINT NOT NULL,
    descripcion                  VARCHAR(250) NOT NULL,
    cantidad                     NUMERIC(10,2) NOT NULL DEFAULT 1,
    precio_unitario              NUMERIC(12,2) NOT NULL,
    descuento                    NUMERIC(12,2) NOT NULL DEFAULT 0,
    importe_total                NUMERIC(12,2) GENERATED ALWAYS AS ((cantidad * precio_unitario) - descuento) STORED,
    estado                       VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_cargo                  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por                   BIGINT NOT NULL,
    motivo_anulacion             VARCHAR(500),
    CONSTRAINT fk_cargos_paciente_tenant
        FOREIGN KEY (id_consultorios, id_pacientes) REFERENCES pacientes(id_consultorios, id_pacientes),
    CONSTRAINT fk_cargos_consulta_tenant
        FOREIGN KEY (id_consultorios, id_consultas) REFERENCES consultas(id_consultorios, id_consultas),
    CONSTRAINT fk_cargos_examen_tenant
        FOREIGN KEY (id_consultorios, id_examenes) REFERENCES examenes(id_consultorios, id_examenes),
    CONSTRAINT fk_cargos_servicio_tenant
        FOREIGN KEY (id_consultorios, id_servicios) REFERENCES servicios(id_consultorios, id_servicios),
    CONSTRAINT fk_cargos_usuario_tenant
        FOREIGN KEY (id_consultorios, creado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_cargos_tenant_id UNIQUE (id_consultorios, id_cargos_servicios),
    CONSTRAINT ck_cargos_descripcion CHECK (btrim(descripcion) <> ''),
    CONSTRAINT ck_cargos_importes CHECK (cantidad > 0 AND precio_unitario >= 0 AND descuento >= 0 AND descuento <= cantidad * precio_unitario),
    CONSTRAINT ck_cargos_estado CHECK (estado IN ('PENDIENTE','PARCIAL','PAGADO','ANULADO')),
    CONSTRAINT ck_cargos_anulacion CHECK (
        (estado <> 'ANULADO' AND motivo_anulacion IS NULL) OR
        (estado = 'ANULADO' AND motivo_anulacion IS NOT NULL)
    )
);
CREATE INDEX ix_cargos_paciente ON cargos_servicios(id_consultorios, id_pacientes, fecha_cargo DESC);
CREATE INDEX ix_cargos_reporte ON cargos_servicios(id_consultorios, fecha_cargo, estado);

CREATE TABLE pagos (
    id_pagos                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pacientes                 BIGINT NOT NULL,
    fecha_pago                   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe                      NUMERIC(12,2) NOT NULL,
    metodo_pago                  VARCHAR(20) NOT NULL,
    numero_operacion             VARCHAR(100),
    observaciones                VARCHAR(500),
    estado                       VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO',
    registrado_por               BIGINT NOT NULL,
    motivo_anulacion             VARCHAR(500),
    fecha_anulacion              TIMESTAMPTZ,
    anulado_por                  BIGINT,
    CONSTRAINT fk_pagos_paciente_tenant
        FOREIGN KEY (id_consultorios, id_pacientes) REFERENCES pacientes(id_consultorios, id_pacientes),
    CONSTRAINT fk_pagos_registrado_tenant
        FOREIGN KEY (id_consultorios, registrado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT fk_pagos_anulado_tenant
        FOREIGN KEY (id_consultorios, anulado_por) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT uq_pagos_tenant_id UNIQUE (id_consultorios, id_pagos),
    CONSTRAINT ck_pagos_importe CHECK (importe > 0),
    CONSTRAINT ck_pagos_metodo CHECK (metodo_pago IN ('EFECTIVO','YAPE','PLIN','TRANSFERENCIA','TARJETA_POS','OTRO')),
    CONSTRAINT ck_pagos_estado CHECK (estado IN ('CONFIRMADO','ANULADO')),
    CONSTRAINT ck_pagos_anulacion CHECK (
        (estado = 'CONFIRMADO' AND motivo_anulacion IS NULL AND fecha_anulacion IS NULL AND anulado_por IS NULL)
        OR
        (estado = 'ANULADO' AND motivo_anulacion IS NOT NULL AND fecha_anulacion IS NOT NULL AND anulado_por IS NOT NULL)
    )
);
CREATE INDEX ix_pagos_reporte_mes ON pagos(id_consultorios, fecha_pago, estado);
CREATE INDEX ix_pagos_paciente ON pagos(id_consultorios, id_pacientes, fecha_pago DESC);

CREATE TABLE pagos_aplicaciones (
    id_pagos_aplicaciones        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_pagos                     BIGINT NOT NULL,
    id_cargos_servicios          BIGINT NOT NULL,
    importe_aplicado             NUMERIC(12,2) NOT NULL,
    fecha_aplicacion             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pagos_aplicaciones_pago_tenant
        FOREIGN KEY (id_consultorios, id_pagos) REFERENCES pagos(id_consultorios, id_pagos),
    CONSTRAINT fk_pagos_aplicaciones_cargo_tenant
        FOREIGN KEY (id_consultorios, id_cargos_servicios) REFERENCES cargos_servicios(id_consultorios, id_cargos_servicios),
    CONSTRAINT uq_pago_cargo UNIQUE (id_consultorios, id_pagos, id_cargos_servicios),
    CONSTRAINT ck_pagos_aplicaciones_importe CHECK (importe_aplicado > 0)
);
CREATE INDEX ix_pagos_aplicaciones_cargo ON pagos_aplicaciones(id_consultorios, id_cargos_servicios);

-- ----------------------------------------------------------------------------
-- 8. Auditoria tecnica
-- ----------------------------------------------------------------------------
CREATE TABLE auditorias (
    id_auditorias                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consultorios              BIGINT NOT NULL,
    id_usuarios                  BIGINT,
    accion                       VARCHAR(40) NOT NULL,
    entidad                      VARCHAR(80) NOT NULL,
    identificador_registro       VARCHAR(80),
    resultado                    VARCHAR(20) NOT NULL,
    direccion_ip                 INET,
    agente_usuario               VARCHAR(500),
    datos_contexto               JSONB,
    fecha_evento                 TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditorias_consultorio
        FOREIGN KEY (id_consultorios) REFERENCES consultorios(id_consultorios),
    CONSTRAINT fk_auditorias_usuario_tenant
        FOREIGN KEY (id_consultorios, id_usuarios) REFERENCES usuarios(id_consultorios, id_usuarios),
    CONSTRAINT ck_auditorias_accion CHECK (btrim(accion) <> ''),
    CONSTRAINT ck_auditorias_entidad CHECK (btrim(entidad) <> ''),
    CONSTRAINT ck_auditorias_resultado CHECK (resultado IN ('EXITOSO','FALLIDO','DENEGADO'))
);
CREATE INDEX ix_auditorias_tenant_fecha ON auditorias(id_consultorios, fecha_evento DESC);
CREATE INDEX ix_auditorias_entidad ON auditorias(id_consultorios, entidad, identificador_registro);

-- ----------------------------------------------------------------------------
-- 9. Triggers uniformes de fecha de modificacion
-- ----------------------------------------------------------------------------
CREATE TRIGGER tg_consultorios_fecha_modificacion
BEFORE UPDATE ON consultorios FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();
CREATE TRIGGER tg_usuarios_fecha_modificacion
BEFORE UPDATE ON usuarios FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();
CREATE TRIGGER tg_pacientes_fecha_modificacion
BEFORE UPDATE ON pacientes FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();
CREATE TRIGGER tg_historias_fecha_modificacion
BEFORE UPDATE ON historias_clinicas FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();
CREATE TRIGGER tg_citas_fecha_modificacion
BEFORE UPDATE ON citas FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();
CREATE TRIGGER tg_consultas_fecha_modificacion
BEFORE UPDATE ON consultas FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();
CREATE TRIGGER tg_examenes_fecha_modificacion
BEFORE UPDATE ON examenes FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();
CREATE TRIGGER tg_servicios_fecha_modificacion
BEFORE UPDATE ON servicios FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

-- ----------------------------------------------------------------------------
-- 10. Vistas de operacion y reportes (sin exponer informacion clinica textual)
-- ----------------------------------------------------------------------------
CREATE VIEW vw_saldos_cargos AS
SELECT
    cs.id_consultorios,
    cs.id_cargos_servicios,
    cs.id_pacientes,
    cs.importe_total,
    COALESCE(SUM(CASE WHEN p.estado = 'CONFIRMADO' THEN pa.importe_aplicado ELSE 0 END), 0)::NUMERIC(12,2) AS importe_pagado,
    (cs.importe_total - COALESCE(SUM(CASE WHEN p.estado = 'CONFIRMADO' THEN pa.importe_aplicado ELSE 0 END), 0))::NUMERIC(12,2) AS saldo_pendiente,
    cs.estado
FROM cargos_servicios cs
LEFT JOIN pagos_aplicaciones pa
    ON pa.id_consultorios = cs.id_consultorios
   AND pa.id_cargos_servicios = cs.id_cargos_servicios
LEFT JOIN pagos p
    ON p.id_consultorios = pa.id_consultorios
   AND p.id_pagos = pa.id_pagos
GROUP BY cs.id_consultorios, cs.id_cargos_servicios, cs.id_pacientes, cs.importe_total, cs.estado;

CREATE VIEW vw_resumen_mensual AS
WITH consultas_mes AS (
    SELECT id_consultorios, date_trunc('month', fecha_hora_atencion) AS periodo,
           COUNT(*) FILTER (WHERE estado = 'CERRADA') AS consultas_atendidas
    FROM consultas
    GROUP BY id_consultorios, date_trunc('month', fecha_hora_atencion)
),
examenes_mes AS (
    SELECT e.id_consultorios, date_trunc('month', e.fecha_realizacion) AS periodo,
           COUNT(*) FILTER (WHERE e.estado IN ('REALIZADO','RESULTADO_RECIBIDO','REVISADO')) AS examenes_realizados,
           COUNT(*) FILTER (WHERE te.categoria = 'LABORATORIO' AND e.estado IN ('REALIZADO','RESULTADO_RECIBIDO','REVISADO')) AS laboratorios,
           COUNT(*) FILTER (WHERE te.categoria = 'ECOGRAFIA' AND e.estado IN ('REALIZADO','RESULTADO_RECIBIDO','REVISADO')) AS ecografias
    FROM examenes e
    JOIN tipos_examenes te
      ON te.id_consultorios = e.id_consultorios
     AND te.id_tipos_examenes = e.id_tipos_examenes
    WHERE e.fecha_realizacion IS NOT NULL
    GROUP BY e.id_consultorios, date_trunc('month', e.fecha_realizacion)
),
pagos_mes AS (
    SELECT id_consultorios, date_trunc('month', fecha_pago) AS periodo,
           COALESCE(SUM(importe) FILTER (WHERE estado = 'CONFIRMADO'), 0)::NUMERIC(14,2) AS total_cobrado
    FROM pagos
    GROUP BY id_consultorios, date_trunc('month', fecha_pago)
),
periodos AS (
    SELECT id_consultorios, periodo FROM consultas_mes
    UNION
    SELECT id_consultorios, periodo FROM examenes_mes
    UNION
    SELECT id_consultorios, periodo FROM pagos_mes
)
SELECT
    pe.id_consultorios,
    pe.periodo,
    COALESCE(cm.consultas_atendidas, 0) AS consultas_atendidas,
    COALESCE(em.examenes_realizados, 0) AS examenes_realizados,
    COALESCE(em.laboratorios, 0) AS laboratorios,
    COALESCE(em.ecografias, 0) AS ecografias,
    COALESCE(pm.total_cobrado, 0)::NUMERIC(14,2) AS total_cobrado
FROM periodos pe
LEFT JOIN consultas_mes cm ON cm.id_consultorios = pe.id_consultorios AND cm.periodo = pe.periodo
LEFT JOIN examenes_mes em ON em.id_consultorios = pe.id_consultorios AND em.periodo = pe.periodo
LEFT JOIN pagos_mes pm ON pm.id_consultorios = pe.id_consultorios AND pm.periodo = pe.periodo;

