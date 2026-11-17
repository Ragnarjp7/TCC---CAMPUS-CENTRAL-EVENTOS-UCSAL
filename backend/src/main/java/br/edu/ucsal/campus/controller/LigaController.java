package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOLigaRequest;
import br.edu.ucsal.campus.dto.DTOLigaResponse;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.LigaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ligas")
public class LigaController {

    private final LigaService ligaService;

    public LigaController(LigaService ligaService) {
        this.ligaService = ligaService;
    }

    @PostMapping
    public ResponseEntity<DTOLigaResponse> criar(
            @RequestBody DTOLigaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ligaService.criar(request, usuario));
    }

    @GetMapping("/publicas")
    public ResponseEntity<List<DTOLigaResponse>> listarPublicas() {

        return ResponseEntity.ok(
                ligaService.listarPublicas());
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<DTOLigaResponse>> listarPendentes(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                ligaService.listarPendentes(usuario));
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<DTOLigaResponse> aprovar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                ligaService.aprovar(id, usuario));
    }

    @PutMapping("/{id}/rejeitar")
    public ResponseEntity<DTOLigaResponse> rejeitar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                ligaService.rejeitar(id, usuario));
    }
}
