package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOConfirmarPresencaAlunoRequest;
import br.edu.ucsal.campus.dto.DTOConfirmarPresencaExternoRequest;
import br.edu.ucsal.campus.dto.DTOPresencaResponse;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.PresencaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventos")
public class PresencaController {

    private final PresencaService presencaService;

    public PresencaController(PresencaService presencaService) {
        this.presencaService = presencaService;
    }

    // Confirma presença de aluno autenticado.
    @PostMapping("/presencas/confirmar")
    public ResponseEntity<DTOPresencaResponse> confirmarPresencaAluno(
            @RequestBody DTOConfirmarPresencaAlunoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        var presenca = presencaService.confirmarPresencaAluno(
                request.token(),
                usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DTOPresencaResponse.de(presenca));
    }

    // Confirma presença de participante externo.
    @PostMapping("/presencas/confirmar/externo")
    public ResponseEntity<DTOPresencaResponse> confirmarPresencaExterno(
            @RequestBody DTOConfirmarPresencaExternoRequest request) {

        var presenca = presencaService.confirmarPresencaExterno(
                request.token(),
                request.nome(),
                request.email());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DTOPresencaResponse.de(presenca));
    }

    @GetMapping("/{eventoId}/presencas")
    public ResponseEntity<List<DTOPresencaResponse>> listarPresencasEvento(
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                presencaService.listarPresencasEvento(
                        eventoId,
                        usuario));
    }
}
