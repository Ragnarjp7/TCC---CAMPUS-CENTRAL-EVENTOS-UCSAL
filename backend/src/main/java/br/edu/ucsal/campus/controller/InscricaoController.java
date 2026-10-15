package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOInscricaoRequest;
import br.edu.ucsal.campus.dto.DTOInscricaoResponse;
import br.edu.ucsal.campus.service.InscricaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/eventos/{eventoId}/inscricoes")
public class InscricaoController {

    private final InscricaoService inscricaoService;

    public InscricaoController(InscricaoService inscricaoService) {
        this.inscricaoService = inscricaoService;
    }

    // Inscrever aluno no evento
    @PostMapping
    public ResponseEntity<DTOInscricaoResponse> inscrever(
            @PathVariable Long eventoId,@RequestBody DTOInscricaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscricaoService.inscrever(eventoId, request.alunoId()));
    }

    // Listar inscritos do evento
    @GetMapping
    public ResponseEntity<List<DTOInscricaoResponse>> listarPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(inscricaoService.listarPorEvento(eventoId));
    }

    // Cancelar inscrição
    @DeleteMapping("/{alunoId}")
    public ResponseEntity<Void> cancelar(@PathVariable Long eventoId,           @PathVariable Long alunoId) {
        inscricaoService.cancelar(eventoId, alunoId);
        return ResponseEntity.noContent().build();
    }
}