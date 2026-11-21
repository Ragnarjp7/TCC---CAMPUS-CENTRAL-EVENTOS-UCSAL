package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOSolicitacaoAjusteLigaRequest;
import br.edu.ucsal.campus.dto.DTOSolicitacaoAjusteLigaResponse;
import br.edu.ucsal.campus.dto.DTORespostaAjusteLigaRequest;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.SolicitacaoAjusteLigaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solicitacoes-ajustes-liga")
public class SolicitacaoAjusteLigaController {

    private final SolicitacaoAjusteLigaService solicitacaoService;

    public SolicitacaoAjusteLigaController(
            SolicitacaoAjusteLigaService solicitacaoService) {

        this.solicitacaoService = solicitacaoService;
    }

    // CAEX ou ADMIN solicita ajustes em uma liga
    @PostMapping("/liga/{ligaId}")
    public ResponseEntity<DTOSolicitacaoAjusteLigaResponse> solicitarAjustes(
            @PathVariable Long ligaId,
            @RequestBody DTOSolicitacaoAjusteLigaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(solicitacaoService.solicitarAjustes(
                        ligaId, request, usuario));
    }

    // Responsável pela liga responde à solicitação
    @PutMapping("/{solicitacaoId}/responder")
    public ResponseEntity<DTOSolicitacaoAjusteLigaResponse> responderAjustes(
            @PathVariable Long solicitacaoId,
            @RequestBody DTORespostaAjusteLigaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                solicitacaoService.responderAjustes(
                        solicitacaoId,
                        request.respostaResponsavel(),
                        usuario));
    }

    // CAEX ou ADMIN conclui a análise dos ajustes
    @PutMapping("/{solicitacaoId}/concluir")
    public ResponseEntity<DTOSolicitacaoAjusteLigaResponse> concluirAjustes(
            @PathVariable Long solicitacaoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                solicitacaoService.concluirAjustes(
                        solicitacaoId, usuario));
    }

    // Consulta o histórico de ajustes de uma liga
    @GetMapping("/liga/{ligaId}")
    public ResponseEntity<List<DTOSolicitacaoAjusteLigaResponse>> listarPorLiga(
            @PathVariable Long ligaId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                solicitacaoService.listarPorLiga(ligaId, usuario));
    }

    // CAEX ou ADMIN lista respostas que aguardam análise
    @GetMapping("/aguardando-analise")
    public ResponseEntity<List<DTOSolicitacaoAjusteLigaResponse>> listarAguardandoAnalise(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                solicitacaoService.listarAguardandoAnalise(usuario));
    }
}
