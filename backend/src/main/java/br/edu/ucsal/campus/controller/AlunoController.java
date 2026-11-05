package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOAlunoResponse;
import br.edu.ucsal.campus.dto.DTOInscricaoResponse;
import br.edu.ucsal.campus.service.AlunoService;
import br.edu.ucsal.campus.service.InscricaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.ucsal.campus.dto.DTOAlunoRequest;

import java.util.List;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;
    private final InscricaoService inscricaoService;

    public AlunoController(AlunoService alunoService, InscricaoService inscricaoService) {
        this.alunoService = alunoService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping
    public ResponseEntity<List<DTOAlunoResponse>> listarTodos() {
        return ResponseEntity.ok(alunoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DTOAlunoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(alunoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<DTOAlunoResponse> criar(@RequestBody DTOAlunoRequest aluno) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alunoService.criar(aluno));
    }

    // Eventos em que o aluno está inscrito
    @GetMapping("/{id}/inscricoes")
    public ResponseEntity<List<DTOInscricaoResponse>> listarInscricoes(@PathVariable Long id) {
        return ResponseEntity.ok(inscricaoService.listarPorAluno(id));
    }
}