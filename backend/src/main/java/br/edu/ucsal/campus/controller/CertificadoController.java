package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.CertificadoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                eventoId,
                usuario
        );

        return ResponseEntity.ok(Map.of(
                "mensagem", "Processamento de certificados concluído.",
                "eventoId", eventoId,
                "quantidadeGerada", quantidade
        ));
    }
}
