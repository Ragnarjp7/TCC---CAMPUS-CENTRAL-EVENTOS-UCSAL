package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.model.Categoria;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.repository.CategoriaRepository;
import br.edu.ucsal.campus.repository.EventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.exception.EventoNaoEncontradoException;
import br.edu.ucsal.campus.dto.DTOStatusRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;

    public EventoService(EventoRepository eventoRepository,
                         CategoriaRepository categoriaRepository) {
        this.eventoRepository = eventoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAllComCategoria();
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
    }

    public List<Evento> listarPorStatus(Evento.StatusEvento status) {
        return eventoRepository.findByStatus(status);
    }

    public List<Evento> listarPorCategoria(Long categoriaId) {
        return eventoRepository.findByCategoriaId(categoriaId);
    }

    @Transactional
    public Evento criar(Evento evento) {
        validarCamposEvento(evento);

        Categoria categoria = categoriaRepository
                .findById(evento.getCategoria().getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        evento.setCategoria(categoria);
        evento.setStatus(Evento.StatusEvento.AGUARDANDO_ANALISE);

        return eventoRepository.save(evento);
    }

    @Transactional
    public Evento atualizarEvento(Long id, Evento dados) {
        Evento evento = eventoRepository.findById(id).orElseThrow(() -> new EventoNaoEncontradoException("Evento não encontrado"));

        validarCamposEvento(dados);

        evento.setTitulo(dados.getTitulo());
        evento.setDescricao(dados.getDescricao());
        evento.setLocal(dados.getLocal());
        evento.setDataInicio(dados.getDataInicio());
        evento.setDataFim(dados.getDataFim());
        evento.setModalidade(dados.getModalidade());
        evento.setCapacidade(dados.getCapacidade());

        Categoria categoria = categoriaRepository
                .findById(dados.getCategoria().getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
        evento.setCategoria(categoria);

        // status NÃO é alterado aqui (deve ser feito só pelo CAEX)
        return eventoRepository.save(evento);
    }

    private static final Map<Evento.StatusEvento, Set<Evento.StatusEvento>> TRANSICOES = Map.of(
        Evento.StatusEvento.AGUARDANDO_ANALISE,
        EnumSet.of(Evento.StatusEvento.APROVADO, Evento.StatusEvento.AJUSTES_SOLICITADOS,Evento.StatusEvento.REJEITADO),

        Evento.StatusEvento.AJUSTES_SOLICITADOS,
        EnumSet.of(Evento.StatusEvento.AGUARDANDO_ANALISE, Evento.StatusEvento.REJEITADO, Evento.StatusEvento.CANCELADO),

        Evento.StatusEvento.APROVADO,
        EnumSet.of(Evento.StatusEvento.CANCELADO, Evento.StatusEvento.ENCERRADO),

        Evento.StatusEvento.REJEITADO, EnumSet.noneOf(Evento.StatusEvento.class),
        Evento.StatusEvento.CANCELADO, EnumSet.noneOf(Evento.StatusEvento.class),
        Evento.StatusEvento.ENCERRADO, EnumSet.noneOf(Evento.StatusEvento.class)
    );

    
@Transactional
public Evento atualizarStatus(Long id, DTOStatusRequest request) {

    if (request == null || request.status() == null) {
        throw new IllegalArgumentException("O status é obrigatório");
    }

    Evento evento = eventoRepository.findById(id)
            .orElseThrow(() ->
                    new EventoNaoEncontradoException("Evento não encontrado"));

    Evento.StatusEvento novoStatus = request.status();
    Evento.StatusEvento atual = evento.getStatus();

    if (!TRANSICOES.getOrDefault(atual, Set.of()).contains(novoStatus)) {
        throw new ConflitoException(
                "Não é possível mudar o status de "
                        + atual + " para " + novoStatus);
    }

    boolean decisaoCaex =
            novoStatus == Evento.StatusEvento.APROVADO
            || novoStatus == Evento.StatusEvento.AJUSTES_SOLICITADOS
            || novoStatus == Evento.StatusEvento.REJEITADO;

    String observacao = request.observacao();

    if (decisaoCaex && (observacao == null || observacao.isBlank())) {
        throw new IllegalArgumentException(
                "A observação é obrigatória para registrar a decisão do CAEX");
    }

    if (decisaoCaex && observacao.length() > 1000) {
        throw new IllegalArgumentException(
                "A observação deve ter no máximo 1000 caracteres");
    }

    if (novoStatus == Evento.StatusEvento.APROVADO && evento.getDataFim().isBefore(LocalDateTime.now())) {
        throw new IllegalArgumentException(
                "Não é possível aprovar um evento que já terminou");
    }

    if (decisaoCaex) {
        evento.setObservacaoCaex(observacao.trim());
    }

    evento.setStatus(novoStatus);
    return eventoRepository.save(evento);
}

    private void validarCamposEvento(Evento e) {
        if (isVazio(e.getTitulo()) || isVazio(e.getDescricao())
                || isVazio(e.getLocal()) || isVazio(e.getModalidade())) {
            throw new IllegalArgumentException(
                    "Título, descrição, local e modalidade são obrigatórios");
        }

        if (e.getCategoria() == null || e.getCategoria().getId() == null) {
            throw new IllegalArgumentException("A categoria é obrigatória");
        }

        if (e.getDataInicio() == null || e.getDataFim() == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias");
        }

        if (!e.getDataFim().isAfter(e.getDataInicio())) {
            throw new IllegalArgumentException("A data de fim deve ser posterior à data de início");
        }

        if (e.getCapacidade() != null && e.getCapacidade() <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser maior que zero");
        }
    }

    private boolean isVazio(String s) {
        return s == null || s.isBlank();
    }

    //Eventos que acontecem em um dia
    public List<Evento> listarPorData(LocalDate data){
        return eventoRepository.findByPedido(data.atStartOfDay(), data.plusDays(1).atStartOfDay());
    }

    //Eventos que acontecem dentro de uma periodo
    public List<Evento> listarPorPeriodo(LocalDate inicio, LocalDate fim){
        if (fim.isBefore(inicio)){
            throw new IllegalArgumentException("A data final deve ser posterior à data inicial");
        }
        return eventoRepository.findByPedido(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
    }

    public List<Evento> listarPublicos() {
    return eventoRepository.findByStatusComCategoria(Evento.StatusEvento.APROVADO);
    }
}