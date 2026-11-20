package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOCargoRequest;
import br.edu.ucsal.campus.dto.DTOCargoResponse;
import br.edu.ucsal.campus.model.Cargo;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.service.CargoService;
import br.edu.ucsal.campus.repository.CargoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ligas/{ligaId}/cargos")
public class CargoController {

    private final CargoService cargoService;
    private final CargoRepository cargoRepository;

    public CargoController(
            CargoService cargoService,
            CargoRepository cargoRepository) {

        this.cargoService = cargoService;
        this.cargoRepository = cargoRepository;
    }

    // Lista os cargos de uma liga
    @GetMapping
    public ResponseEntity<List<DTOCargoResponse>> listar(
            @PathVariable Long ligaId) {

        List<DTOCargoResponse> cargos =
                cargoRepository.findByLigaId(ligaId)
                        .stream()
                        .map(DTOCargoResponse::de)
                        .toList();

        return ResponseEntity.ok(cargos);
    }

    // Cria um cargo
    @PostMapping
    public ResponseEntity<DTOCargoResponse> criar(
            @PathVariable Long ligaId,
            @RequestBody DTOCargoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Cargo cargo = cargoService.criar(
                ligaId, request, usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DTOCargoResponse.de(cargo));
    }

    // Edita um cargo
    @PutMapping("/{cargoId}")
    public ResponseEntity<DTOCargoResponse> atualizar(
            @PathVariable Long ligaId,
            @PathVariable Long cargoId,
            @RequestBody DTOCargoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Cargo cargo = cargoService.atualizar(
                ligaId, cargoId, request, usuario);

        return ResponseEntity.ok(DTOCargoResponse.de(cargo));
    }
}
