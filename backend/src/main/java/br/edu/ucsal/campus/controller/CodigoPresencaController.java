package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOCodigoPresencaResponse;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.CodigoPresencaService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eventos")
public class CodigoPresencaController {

    private final CodigoPresencaService codigoPresencaService;

    public CodigoPresencaController(
            CodigoPresencaService codigoPresencaService) {

        this.codigoPresencaService = codigoPresencaService;
    }

    @PostMapping("/{eventoId}/codigo-presenca")
    public ResponseEntity<DTOCodigoPresencaResponse> gerarOuRenovar(
            @PathVariable Long eventoId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        DTOCodigoPresencaResponse resposta =
                codigoPresencaService.gerarOuRenovar(
                        eventoId,
                        usuario);

        return ResponseEntity.ok(resposta);
    }
}
