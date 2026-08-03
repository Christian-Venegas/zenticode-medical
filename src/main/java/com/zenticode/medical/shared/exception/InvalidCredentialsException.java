package com.zenticode.medical.shared.exception;

/**
 * Representa credenciales de acceso no válidas.
 */
public class InvalidCredentialsException
        extends RuntimeException {

    // Código estable utilizado por el frontend.
    private static final String CODIGO =
            "INVALID_CREDENTIALS";

    // Mensaje seguro que no revela qué credencial falló.
    private static final String MENSAJE =
            "Las credenciales proporcionadas no son válidas.";

    // Crea una excepción de autenticación segura.
    public InvalidCredentialsException() {
        super(MENSAJE);
    }

    public String getCodigo() {
        return CODIGO;
    }
}