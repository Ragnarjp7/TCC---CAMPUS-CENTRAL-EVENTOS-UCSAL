
package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOMembroLigaResponse;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.MembroLigaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ligas")
public class MembroLigaController {

    private final MembroLigaService membroLigaService;

    public MembroLigaController(MembroLigaService membroLigaService) {
        this.membroLigaService = membroLigaService;
    }

    // Solicita participação em uma liga
    @PostMapping("/{ligaId}/solicitar-participacao")
    public ResponseEntity<DTOMembroLigaResponse> solicitarParticipacao(
            @PathVariable Long ligaId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        MembroLiga membro = membroLigaService.solicitarParticipacao(
                ligaId, usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DTOMembroLigaResponse.de(membro));
    }

    // Lista solicitações pendentes de uma liga
    @GetMapping("/{ligaId}/solicitacoes")
    public ResponseEntity<List<DTOMembroLigaResponse>> listarSolicitacoes(
            @PathVariable Long ligaId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        List<DTOMembroLigaResponse> solicitacoes =
                membroLigaService.listarSolicitacoesPendentes(ligaId, usuario)
                        .stream()
                        .map(DTOMembroLigaResponse::de)
                        .toList();

        return ResponseEntity.ok(solicitacoes);
    }

    // Aprova uma solicitação
    @PutMapping("/{ligaId}/membros/{membroId}/aprovar")
    public ResponseEntity<DTOMembroLigaResponse> aprovar(
            @PathVariable Long ligaId,
            @PathVariable Long membroId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        MembroLiga membro = membroLigaService.aprovarSolicitacao(
                ligaId, membroId, usuario);

        return ResponseEntity.ok(DTOMembroLigaResponse.de(membro));
    }

    // Recusa uma solicitação
    @PutMapping("/{ligaId}/membros/{membroId}/recusar")
    public ResponseEntity<DTOMembroLigaResponse> recusar(
            @PathVariable Long ligaId,
            @PathVariable Long membroId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        MembroLiga membro = membroLigaService.recusarSolicitacao(
                ligaId, membroId, usuario);

        return ResponseEntity.ok(DTOMembroLigaResponse.de(membro));
    }
}
