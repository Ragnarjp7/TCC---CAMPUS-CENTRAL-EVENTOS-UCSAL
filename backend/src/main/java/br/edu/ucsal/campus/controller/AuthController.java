package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOLoginRequest;
import br.edu.ucsal.campus.dto.DTOLoginResponse;
import br.edu.ucsal.campus.dto.DTOMeResponse;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<DTOLoginResponse> login(@RequestBody DTOLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<DTOMeResponse> me(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(authService.buscarUsuario(usuario.id()));
    }
}