package com.zenticode.medical.auth.controller;

import com.zenticode.medical.auth.dto.LoginRequest;
import com.zenticode.medical.auth.dto.LoginResponse;
import com.zenticode.medical.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Expone las operaciones de autenticación.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    // Inyecta el servicio de autenticación.
    public AuthController(
            final AuthService authService
    ) {
        this.authService = Objects.requireNonNull(
                authService,
                "El servicio de autenticación es obligatorio."
        );
    }

    // Valida las credenciales del usuario.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(
            @Valid
            @RequestBody
            final LoginRequest solicitud
    ) {
        final LoginResponse respuesta =
                authService.iniciarSesion(solicitud);

        return ResponseEntity.ok(respuesta);
    }
}