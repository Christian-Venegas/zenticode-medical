package com.zenticode.medical.shared.exception;

/**
 * Representa un conflicto con información existente.
 */
public class BusinessConflictException
        extends IllegalArgumentException {

    // Código estable utilizado por el frontend.
    private final String codigo;

    // Crea un conflicto con un código y mensaje seguros.
    public BusinessConflictException(
            final String codigo,
            final String mensaje
    ) {
        super(validarMensaje(mensaje));
        this.codigo = validarCodigo(codigo);
    }

    public String getCodigo() {
        return codigo;
    }

    // Comprueba y normaliza el código del conflicto.
    private static String validarCodigo(
            final String codigo
    ) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del conflicto es obligatorio."
            );
        }

        return codigo.trim();
    }

    // Comprueba y normaliza el mensaje del conflicto.
    private static String validarMensaje(
            final String mensaje
    ) {
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje del conflicto es obligatorio."
            );
        }

        return mensaje.trim();
    }
}