package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOAlunoRequest;
import br.edu.ucsal.campus.dto.DTOAlunoResponse;
import br.edu.ucsal.campus.dto.DTOInscricaoResponse;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.AlunoService;
import br.edu.ucsal.campus.service.InscricaoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;
    private final InscricaoService inscricaoService;

    public AlunoController(
            AlunoService alunoService,
            InscricaoService inscricaoService) {

        this.alunoService = alunoService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping
    public ResponseEntity<List<DTOAlunoResponse>> listarTodos() {
        return ResponseEntity.ok(alunoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DTOAlunoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(alunoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<DTOAlunoResponse> criar(
            @RequestBody DTOAlunoRequest aluno) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alunoService.criar(aluno));
    }

    // Consulta inscrições de outro aluno (somente CAEX ou ADMIN)
    @GetMapping("/{id}/inscricoes")
    public ResponseEntity<List<DTOInscricaoResponse>> listarInscricoes(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (usuario == null
                || usuario.papeis() == null
                || (!usuario.papeis().contains(Papel.CAEX)
                && !usuario.papeis().contains(Papel.ADMIN))) {

            throw new AccessDeniedException(
                    "Você não tem permissão para consultar as inscrições deste aluno");
        }

        return ResponseEntity.ok(inscricaoService.listarPorAluno(id));
    }

    // Perfil do aluno logado
    @GetMapping("/me")
    public ResponseEntity<DTOAlunoResponse> meuPerfil(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                alunoService.buscarPorUsuarioId(usuario.id())
        );
    }

    // Inscrições do próprio aluno logado
    @GetMapping("/me/inscricoes")
    public ResponseEntity<List<DTOInscricaoResponse>> minhasInscricoes(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Long alunoId = alunoService.buscarIdPorUsuario(usuario.id());

        return ResponseEntity.ok(
                inscricaoService.listarPorAluno(alunoId)
        );
    }
}
