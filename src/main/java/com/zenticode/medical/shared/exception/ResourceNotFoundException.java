package com.zenticode.medical.shared.exception;

/**
 * Representa un recurso solicitado que no fue encontrado.
 */
public class ResourceNotFoundException
        extends IllegalArgumentException {

    // Código estable utilizado por el frontend.
    private final String codigo;

    // Crea una excepción con código y mensaje seguros.
    public ResourceNotFoundException(
            final String codigo,
            final String mensaje
    ) {
        super(validarMensaje(mensaje));
        this.codigo = validarCodigo(codigo);
    }

    public String getCodigo() {
        return codigo;
    }

    // Comprueba y normaliza el código.
    private static String validarCodigo(
            final String codigo
    ) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del recurso no encontrado es obligatorio."
            );
        }

        return codigo.trim();
    }

    // Comprueba y normaliza el mensaje.
    private static String validarMensaje(
            final String mensaje
    ) {
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje del recurso no encontrado es obligatorio."
            );
        }

        return mensaje.trim();
    }
}