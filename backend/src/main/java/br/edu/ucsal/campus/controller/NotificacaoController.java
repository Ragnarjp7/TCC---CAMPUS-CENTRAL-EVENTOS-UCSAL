package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTONotificacaoResponse;
import br.edu.ucsal.campus.model.Notificacao;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.NotificacaoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    // Lista todas as notificações do usuário autenticado
    @GetMapping
    public ResponseEntity<List<DTONotificacaoResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        List<DTONotificacaoResponse> notificacoes =
                notificacaoService.listarPorUsuario(usuario.id())
                        .stream()
                        .map(DTONotificacaoResponse::de)
                        .toList();

        return ResponseEntity.ok(notificacoes);
    }

    // Consulta a quantidade de notificações não lidas
    @GetMapping("/nao-lidas/quantidade")
    public ResponseEntity<Long> contarNaoLidas(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                notificacaoService.contarNaoLidas(usuario.id())
        );
    }

    // Marca uma notificação como lida
    @PatchMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        notificacaoService.marcarComoLida(id, usuario.id());

        return ResponseEntity.noContent().build();
    }
}