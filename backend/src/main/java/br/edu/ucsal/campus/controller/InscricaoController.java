package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOInscricaoResponse;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.AlunoService;
import br.edu.ucsal.campus.service.InscricaoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventos/{eventoId}/inscricoes")
public class InscricaoController {

    private final InscricaoService inscricaoService;
    private final AlunoService alunoService;

    public InscricaoController(
            InscricaoService inscricaoService,
            AlunoService alunoService) {

        this.inscricaoService = inscricaoService;
        this.alunoService = alunoService;
    }

    // O aluno logado se inscreve no evento
    @PostMapping
    public ResponseEntity<DTOInscricaoResponse> inscrever(
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Long alunoId = alunoService.buscarIdPorUsuario(usuario.id());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscricaoService.inscrever(eventoId, alunoId));
    }

    // Lista os inscritos do evento (CAEX ou ADMIN)
    @GetMapping
    public ResponseEntity<List<DTOInscricaoResponse>> listarPorEvento(
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                inscricaoService.listarPorEvento(eventoId, usuario));
    }

    // O aluno logado cancela a própria inscrição
    @DeleteMapping
    public ResponseEntity<Void> cancelar(
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Long alunoId = alunoService.buscarIdPorUsuario(usuario.id());

        inscricaoService.cancelar(eventoId, alunoId);

        return ResponseEntity.noContent().build();
    }
}
