package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOPublicacaoRequest;
import br.edu.ucsal.campus.dto.DTOPublicacaoResponse;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.PublicacaoService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/publicacoes")
public class PublicacaoController {

    private final PublicacaoService publicacaoService;

    public PublicacaoController(PublicacaoService publicacaoService) {
        this.publicacaoService = publicacaoService;
    }

    // Criar publicação
    @PostMapping
    public ResponseEntity<DTOPublicacaoResponse> criar(
            @RequestBody DTOPublicacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(publicacaoService.criar(request, usuario));
    }

    // Editar publicação
    @PutMapping("/{id}")
    public ResponseEntity<DTOPublicacaoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody DTOPublicacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                publicacaoService.atualizar(id, request, usuario)
        );
    }

    // Excluir publicação
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        publicacaoService.excluir(id, usuario);

        return ResponseEntity.noContent().build();
    }

    // Feed institucional
    @GetMapping("/feed")
    public ResponseEntity<Page<DTOPublicacaoResponse>> listarFeed(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            Pageable pageable) {

        return ResponseEntity.ok(
                publicacaoService.listarFeed(usuario, pageable)
        );
    }

    // Publicações do usuário autenticado
    @GetMapping("/minhas")
    public ResponseEntity<Page<DTOPublicacaoResponse>> listarMinhas(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            Pageable pageable) {

        return ResponseEntity.ok(
                publicacaoService.listarMinhas(usuario, pageable)
        );
    }

    // Consultar uma publicação
    @GetMapping("/{id}")
    public ResponseEntity<DTOPublicacaoResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                publicacaoService.buscarPorId(id, usuario)
        );
    }
}
