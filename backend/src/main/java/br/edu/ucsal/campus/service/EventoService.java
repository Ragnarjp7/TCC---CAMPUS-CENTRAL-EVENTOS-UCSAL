package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.entity.Categoria;
import br.edu.ucsal.campus.entity.Evento;
import br.edu.ucsal.campus.repository.CategoriaRepository;
import br.edu.ucsal.campus.repository.EventoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;

    public EventoService(
            EventoRepository eventoRepository,
            CategoriaRepository categoriaRepository) {
        this.eventoRepository = eventoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Evento não encontrado"));
    }

    public List<Evento> listarPorStatus(String status) {
        return eventoRepository.findByStatusIgnoreCase(status);
    }

    public List<Evento> listarPorCategoria(Long categoriaId) {
        return eventoRepository.findByCategoriaId(categoriaId);
    }

    public Evento criar(Evento evento) {
        validarEvento(evento);

        Long categoriaId = evento.getCategoria().getId();

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Categoria não encontrada"));

        evento.setCategoria(categoria);

        return eventoRepository.save(evento);
    }

    private void validarEvento(Evento evento) {
        if (evento.getDataInicio() == null
                || evento.getDataFim() == null) {
            throw new IllegalArgumentException(
                    "As datas de início e fim são obrigatórias");
        }

        if (!evento.getDataFim().isAfter(evento.getDataInicio())) {
            throw new IllegalArgumentException(
                    "A data de fim deve ser posterior à data de início");
        }

        if (evento.getCapacidade() != null
                && evento.getCapacidade() <= 0) {
            throw new IllegalArgumentException(
                    "A capacidade deve ser maior que zero");
        }
    }
}
