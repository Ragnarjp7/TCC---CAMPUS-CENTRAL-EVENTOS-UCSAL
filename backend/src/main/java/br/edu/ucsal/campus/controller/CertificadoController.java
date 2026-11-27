package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOCertificadoResponse;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.CertificadoService;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/certificados")
public class CertificadoController {

    private final CertificadoService certificadoService;

    public CertificadoController(CertificadoService certificadoService) {
        this.certificadoService = certificadoService;
    }

    @PostMapping("/eventos/{eventoId}/gerar")
    public ResponseEntity<Map<String, Object>> gerarCertificados(
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        int quantidade = certificadoService.gerarCertificadosEvento(
                eventoId, usuario);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Processamento de certificados concluído.",
                "eventoId", eventoId,
                "quantidadeGerada", quantidade
        ));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<DTOCertificadoResponse>> listarMeusCertificados(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                certificadoService.listarMeusCertificados(usuario));
    }

    @GetMapping("/eventos/{eventoId}")
    public ResponseEntity<List<DTOCertificadoResponse>> listarCertificadosEvento(
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                certificadoService.listarCertificadosEvento(
                        eventoId, usuario));
    }

    @GetMapping("/{certificadoId}/download")
    public ResponseEntity<ByteArrayResource> baixarCertificado(
            @PathVariable Long certificadoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return certificadoService.baixarCertificado(
                certificadoId, usuario);
    }

    @PostMapping("/eventos/{eventoId}/reenviar")
    public ResponseEntity<Map<String, Object>> reenviarCertificados(
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        int quantidade = certificadoService.reenviarCertificadosPendentes(
                eventoId, usuario);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Processamento de reenvio concluído.",
                "eventoId", eventoId,
                "quantidadeEnviada", quantidade
        ));
    }
}