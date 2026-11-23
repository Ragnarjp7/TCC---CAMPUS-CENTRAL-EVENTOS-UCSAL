package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOInscricaoResponse;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.exception.EventoNaoEncontradoException;
import br.edu.ucsal.campus.exception.NaoEncontradoException;
import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.Inscricao;
import br.edu.ucsal.campus.model.Inscricao.StatusInscricao;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.EventoRepository;
import br.edu.ucsal.campus.repository.InscricaoRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final EventoRepository eventoRepository;
    private final AlunoRepository alunoRepository;
    private final AuditoriaService auditoriaService;

    public InscricaoService(
            InscricaoRepository inscricaoRepository,
            EventoRepository eventoRepository,
            AlunoRepository alunoRepository,
            AuditoriaService auditoriaService) {

        this.inscricaoRepository = inscricaoRepository;
        this.eventoRepository = eventoRepository;
        this.alunoRepository = alunoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public DTOInscricaoResponse inscrever(
            Long eventoId,
            Long alunoId,
            UsuarioAutenticado usuario) {

        if (alunoId == null) {
            throw new IllegalArgumentException(
                    "O ID do aluno é obrigatório");
        }

        if (usuario == null) {
            throw new AccessDeniedException(
                    "Usuário não autenticado");
        }

        // Bloqueia o evento durante a operação para controlar
        // inscrições simultâneas e evitar ultrapassar a capacidade.
        Evento evento = eventoRepository.findByIdParaInscricao(eventoId)
                .orElseThrow(() ->
                        new EventoNaoEncontradoException(
                                "Evento não encontrado"));

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() ->
                        new NaoEncontradoException(
                                "Aluno não encontrado"));

        if (evento.getStatus() != Evento.StatusEvento.APROVADO) {
            throw new ConflitoException(
                    "Só é possível se inscrever em eventos aprovados");
        }

        if (evento.getDataFim() == null || evento.getHoraFim() == null
        || evento.getDataFim().atTime(evento.getHoraFim())
                .isBefore(LocalDateTime.now())) {

         throw new ConflitoException(
            "Este evento já foi encerrado");
}

        boolean inscricaoAtiva = inscricaoRepository
                .existsByAlunoIdAndEventoIdAndStatus(
                        alunoId,
                        eventoId,
                        StatusInscricao.ATIVA);

        if (inscricaoAtiva) {
            throw new ConflitoException(
                    "O aluno já possui uma inscrição ativa neste evento");
        }

        long totalInscricoesAtivas =
                inscricaoRepository.countByEventoIdAndStatus(
                        eventoId,
                        StatusInscricao.ATIVA);

        Integer limiteVagas = evento.getLimiteVagas();

        if (limiteVagas != null && totalInscricoesAtivas >= limiteVagas) {

            throw new ConflitoException(
                    "O evento atingiu a capacidade máxima");
        }

        // Cada nova inscrição gera um registro independente,
        // preservando as tentativas anteriores no histórico.
        Inscricao novaInscricao = new Inscricao(aluno, evento);

        Inscricao salva = inscricaoRepository.save(novaInscricao);

        auditoriaService.registrar(
                usuario,
                "REALIZAR_INSCRICAO",
                "INSCRICAO",
                salva.getId(),
                null,
                "Status: ATIVA",
                "Inscrição realizada no evento: " + evento.getTitulo()
        );

        return DTOInscricaoResponse.de(salva);
    }

    @Transactional
    public void cancelar(
            Long eventoId,
            Long alunoId,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new AccessDeniedException(
                    "Usuário não autenticado");
        }

        Inscricao inscricao = inscricaoRepository
                .findFirstByAlunoIdAndEventoIdAndStatusOrderByDataInscricaoDesc(
                        alunoId,
                        eventoId,
                        StatusInscricao.ATIVA)
                .orElseThrow(() ->
                        new NaoEncontradoException(
                                "Inscrição ativa não encontrada"));

        inscricao.cancelar();

        auditoriaService.registrar(
                usuario,
                "CANCELAR_INSCRICAO",
                "INSCRICAO",
                inscricao.getId(),
                "Status: ATIVA",
                "Status: CANCELADA",
                "Inscrição cancelada no evento: "
                        + inscricao.getEvento().getTitulo()
        );
    }

    // Lista todas as inscrições do evento, incluindo canceladas.
    // A consulta é restrita aos perfis CAEX e ADMIN.
    @Transactional(readOnly = true)
    public List<DTOInscricaoResponse> listarPorEvento(
            Long eventoId,
            UsuarioAutenticado usuario) {

        validarPermissaoConsulta(usuario);

        if (!eventoRepository.existsById(eventoId)) {
            throw new EventoNaoEncontradoException(
                    "Evento não encontrado");
        }

        return inscricaoRepository.findByEventoIdCompleto(eventoId)
                .stream()
                .map(DTOInscricaoResponse::de)
                .toList();
    }

    // Retorna o histórico de inscrições de um aluno.
    @Transactional(readOnly = true)
    public List<DTOInscricaoResponse> listarPorAluno(Long alunoId) {

        if (alunoId == null
                || !alunoRepository.existsById(alunoId)) {

            throw new NaoEncontradoException(
                    "Aluno não encontrado");
        }

        return inscricaoRepository.findByAlunoIdCompleto(alunoId)
                .stream()
                .map(DTOInscricaoResponse::de)
                .toList();
    }

    private void validarPermissaoConsulta(
            UsuarioAutenticado usuario) {

        if (usuario == null
                || usuario.papeis() == null
                || (!usuario.papeis().contains(Papel.CAEX)
                && !usuario.papeis().contains(Papel.ADMIN))) {

            throw new AccessDeniedException(
                    "Somente CAEX ou ADMIN podem visualizar "
                            + "as inscrições do evento");
        }
    }
}
