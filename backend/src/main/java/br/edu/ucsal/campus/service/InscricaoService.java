package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOInscricaoResponse;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.exception.EventoNaoEncontradoException;
import br.edu.ucsal.campus.exception.NaoEncontradoException;
import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.Inscricao;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.EventoRepository;
import br.edu.ucsal.campus.repository.InscricaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final EventoRepository eventoRepository;
    private final AlunoRepository alunoRepository;

    public InscricaoService(InscricaoRepository inscricaoRepository, EventoRepository eventoRepository, AlunoRepository alunoRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.eventoRepository = eventoRepository;
        this.alunoRepository = alunoRepository;
    }

    @Transactional
    public DTOInscricaoResponse inscrever(Long eventoId, Long alunoId) {
        if (alunoId == null) {
            throw new IllegalArgumentException("O alunoId é obrigatório");
        }

        Evento evento = eventoRepository.findByIdParaInscricao(eventoId).orElseThrow(() -> new EventoNaoEncontradoException("Evento não encontrado"));

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new NaoEncontradoException("Aluno não encontrado"));

        if (evento.getStatus() != Evento.StatusEvento.APROVADO) {
            throw new IllegalArgumentException(
                    "Só é possível se inscrever em eventos aprovados");
        }

        if (evento.getDataFim().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Este evento já foi encerrado");
        }

        if (inscricaoRepository.existsByAlunoIdAndEventoId(alunoId, eventoId)) {
            throw new ConflitoException("O aluno já está inscrito neste evento");
        }

        if (evento.getCapacidade() != null
                && inscricaoRepository.countByEventoId(eventoId) >= evento.getCapacidade()) {
            throw new ConflitoException("O evento atingiu a capacidade máxima");
        }

        Inscricao salva = inscricaoRepository.save(new Inscricao(aluno, evento));
        return DTOInscricaoResponse.de(salva);
    }

    @Transactional
    public void cancelar(Long eventoId, Long alunoId) {
        Inscricao inscricao = inscricaoRepository.findByAlunoIdAndEventoId(alunoId, eventoId).orElseThrow(() -> new NaoEncontradoException("Inscrição não encontrada"));

        inscricaoRepository.delete(inscricao);
    }

    @Transactional(readOnly = true)
    public List<DTOInscricaoResponse> listarPorEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new EventoNaoEncontradoException("Evento não encontrado");
        }
        return inscricaoRepository.findByEventoIdCompleto(eventoId)
                .stream().map(DTOInscricaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<DTOInscricaoResponse> listarPorAluno(Long alunoId) {
        if (!alunoRepository.existsById(alunoId)) {
            throw new NaoEncontradoException("Aluno não encontrado");
        }
        return inscricaoRepository.findByAlunoIdCompleto(alunoId)
                .stream().map(DTOInscricaoResponse::de).toList();
    }
}