package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOEventoStatusResponse;
import br.edu.ucsal.campus.dto.DTOStatusRequest;
import br.edu.ucsal.campus.dto.DTOEventoRequest;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.service.EventoService;
import br.edu.ucsal.campus.security.UsuarioAutenticado;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDate;
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
        return ResponseEntity.ok(eventoService.listarPorStatus(Evento.StatusEvento.valueOf(status.toUpperCase())));
    }

    // Listar eventos por categoria
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Evento>> listarPorCategoria(
            @PathVariable Long categoriaId) {
        return ResponseEntity.ok(eventoService.listarPorCategoria(categoriaId));
    }

    //Buscar evento por data
    @GetMapping ("/data")
    public ResponseEntity<List<Evento>> listarPorData(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(eventoService.listarPorData(data));
    }

    @GetMapping ("/periodo")
    public ResponseEntity<List<Evento>> listarPorPeriodo(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim){

        return ResponseEntity.ok(eventoService.listarPorPeriodo(inicio, fim));
    }

    // Visitante (sem login): somente eventos aprovados
    @GetMapping("/publicos")
    public ResponseEntity<List<Evento>> listarPublicos() {
    return ResponseEntity.ok(eventoService.listarPublicos());
    }

    // Cadastrar evento pela liga
@PostMapping
public ResponseEntity<Evento> criar(@RequestBody DTOEventoRequest request,@AuthenticationPrincipal UsuarioAutenticado usuario) {

    Evento novoEvento = eventoService.criar(request, usuario);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(novoEvento);
}

    //Atualizar dados evento
    @PutMapping("/{id}")
public ResponseEntity<Evento> atualizarEvento(@PathVariable Long id, @RequestBody Evento eventoAtualizado, @AuthenticationPrincipal UsuarioAutenticado usuario) {
    
    return ResponseEntity.ok(
            eventoService.atualizarEvento(id, eventoAtualizado, usuario)
    );
}

    //Atualizar status do evento (Deve ser feito apenas pelo CAEX)
    @PutMapping("/{id}/status")
    public ResponseEntity<DTOEventoStatusResponse> atualizarStatus(@PathVariable Long id,@RequestBody DTOStatusRequest request) {
        
        Evento atualizado = eventoService.atualizarStatus(id, request);
        return ResponseEntity.ok(DTOEventoStatusResponse.de(atualizado));
    }

    
}
