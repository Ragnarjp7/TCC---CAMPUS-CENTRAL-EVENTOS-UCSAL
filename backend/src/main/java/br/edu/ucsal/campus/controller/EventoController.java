package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.service.EventoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    // Listar todos os eventos
    @GetMapping
    public ResponseEntity<List<Evento>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    // Buscar evento por ID
    @GetMapping("/{id}")
    public ResponseEntity<Evento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    // Listar eventos por status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Evento>> listarPorStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(eventoService.listarPorStatus(status));
    }

    // Listar eventos por categoria
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Evento>> listarPorCategoria(
            @PathVariable Long categoriaId) {
        return ResponseEntity.ok(
                eventoService.listarPorCategoria(categoriaId));
    }

    // Cadastrar evento
    @PostMapping
    public ResponseEntity<Evento> criar(@RequestBody Evento evento) {
        Evento novoEvento = eventoService.criar(evento);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(novoEvento);
    }
}
