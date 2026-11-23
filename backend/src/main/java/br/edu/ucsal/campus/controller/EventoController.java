package br.edu.ucsal.campus.controller;

import br.edu.ucsal.campus.dto.DTOEventoAnaliseResponse;
import br.edu.ucsal.campus.dto.DTOEventoStatusResponse;
import br.edu.ucsal.campus.dto.DTOStatusRequest;
import br.edu.ucsal.campus.dto.DTOEventoRequest;
import br.edu.ucsal.campus.dto.DTOEventoResponse;
import br.edu.ucsal.campus.dto.DTOPagina;
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
    public ResponseEntity<List<DTOEventoResponse>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    // Buscar evento por ID respeitando as regras de acesso
    @GetMapping("/{id}")
    public ResponseEntity<DTOEventoResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                eventoService.buscarPorId(id, usuario)
        );
    }

    // Listar eventos por status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DTOEventoResponse>> listarPorStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                eventoService.listarPorStatus(
                        Evento.StatusEvento.valueOf(status.toUpperCase())
                )
        );
    }

    // Listar eventos por categoria
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<DTOEventoResponse>> listarPorCategoria(
            @PathVariable Long categoriaId) {

        return ResponseEntity.ok(
                eventoService.listarPorCategoria(categoriaId)
        );
    }

    // Buscar eventos por data
    @GetMapping("/data")
    public ResponseEntity<List<DTOEventoResponse>> listarPorData(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data) {

        return ResponseEntity.ok(
                eventoService.listarPorData(data)
        );
    }

    // Buscar eventos por período
    @GetMapping("/periodo")
    public ResponseEntity<List<DTOEventoResponse>> listarPorPeriodo(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim) {

        return ResponseEntity.ok(
                eventoService.listarPorPeriodo(inicio, fim)
        );
    }

    // RF31/RF33 - Eventos disponíveis (aprovados e não encerrados), abertos a visitantes.
    // Todos os filtros são opcionais e podem ser combinados:
    // busca (título, descrição, local ou liga), categoriaId, modalidade e período (inicio/fim).
    @GetMapping("/publicos")
    public ResponseEntity<DTOPagina<DTOEventoResponse>> listarPublicos(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String modalidade,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                eventoService.buscarDisponiveis(
                        busca, categoriaId, modalidade, inicio, fim, page, size, usuario)
        );
    }

    // RF27 - Cadastrar evento (presidência de liga, professor ou coordenador).
    // O evento entra na fila de análise do CAEX.
    @PostMapping
    public ResponseEntity<DTOEventoAnaliseResponse> criar(
            @RequestBody DTOEventoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventoService.criar(request, usuario));
    }

    // RF27 - Eventos pelos quais o usuário logado responde, com a situação na análise
    @GetMapping("/meus")
    public ResponseEntity<List<DTOEventoAnaliseResponse>> listarMeus(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(eventoService.listarDoResponsavel(usuario));
    }

    // RF29 - Fila de análise do CAEX (padrão: AGUARDANDO_ANALISE)
    @GetMapping("/analise")
    public ResponseEntity<List<DTOEventoAnaliseResponse>> listarParaAnalise(
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                eventoService.listarParaAnalise(
                        status == null || status.isBlank()
                                ? null
                                : Evento.StatusEvento.valueOf(status.trim().toUpperCase())
                )
        );
    }

    // RF29 - Proposta completa do evento (CAEX/ADMIN ou quem responde pelo evento)
    @GetMapping("/{id}/analise")
    public ResponseEntity<DTOEventoAnaliseResponse> buscarParaAnalise(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(eventoService.buscarParaAnalise(id, usuario));
    }

    // Reenviar o evento ao CAEX após solicitação de ajustes
    @PutMapping("/{id}")
    public ResponseEntity<DTOEventoAnaliseResponse> atualizarEvento(
            @PathVariable Long id,
            @RequestBody DTOEventoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(
                eventoService.atualizarEvento(id, request, usuario)
        );
    }

    // Atualizar status do evento (somente CAEX e ADMIN)
    @PutMapping("/{id}/status")
    public ResponseEntity<DTOEventoStatusResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestBody DTOStatusRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Evento atualizado = eventoService.atualizarStatus(
                id,
                request,
                usuario
        );

        return ResponseEntity.ok(
                DTOEventoStatusResponse.de(atualizado)
        );
    }
}
