-- Registra los roles base del sistema.
INSERT INTO roles (
    codigo,
    nombre,
    descripcion,
    activo
)
VALUES
    (
        'ADMIN_CONSULTORIO',
        'Administrador del consultorio',
        'Administra usuarios, configuración y operaciones del consultorio.',
        TRUE
    ),
    (
        'MEDICO',
        'Médico',
        'Gestiona pacientes, consultas e información clínica.',
        TRUE
    ),
    (
        'ASISTENTE',
        'Asistente',
        'Gestiona citas y operaciones administrativas permitidas.',
        TRUE
    )
ON CONFLICT (codigo)
DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    activo = TRUE;