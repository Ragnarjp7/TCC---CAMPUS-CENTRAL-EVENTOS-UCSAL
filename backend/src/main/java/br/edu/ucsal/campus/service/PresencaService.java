package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.CodigoPresenca;
import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.Inscricao;
import br.edu.ucsal.campus.model.Presenca;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.CodigoPresencaRepository;
import br.edu.ucsal.campus.repository.InscricaoRepository;
import br.edu.ucsal.campus.repository.PresencaRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;
import br.edu.ucsal.campus.dto.DTOPresencaResponse;
import br.edu.ucsal.campus.repository.EventoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;

@Service
public class PresencaService {

    private final CodigoPresencaRepository codigoPresencaRepository;
    private final PresencaRepository presencaRepository;
    private final InscricaoRepository inscricaoRepository;
    private final AlunoRepository alunoRepository;
    private final EventoRepository eventoRepository;

    public PresencaService(
            EventoRepository eventoRepository,
            CodigoPresencaRepository codigoPresencaRepository,
            PresencaRepository presencaRepository,
            InscricaoRepository inscricaoRepository,
            AlunoRepository alunoRepository) {

        this.eventoRepository = eventoRepository;
        this.codigoPresencaRepository = codigoPresencaRepository;
        this.presencaRepository = presencaRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.alunoRepository = alunoRepository;
    }

    // Confirma presença de um aluno autenticado.
    // Exige inscrição ativa no evento.
    @Transactional
    public Presenca confirmarPresencaAluno(
            String token,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new ConflitoException(
                    "É necessário estar autenticado para confirmar presença");
        }

        if (usuario.papeis() == null
                || !usuario.papeis().contains(Papel.ALUNO)) {

            throw new ConflitoException(
                    "Somente alunos podem confirmar presença por esta modalidade");
        }

        CodigoPresenca codigo = buscarCodigoAtivo(token);
        Evento evento = codigo.getEvento();

        validarPeriodoEvento(evento);

        Aluno aluno = alunoRepository.findByUsuarioId(usuario.id())
                .orElseThrow(() -> new ConflitoException(
                        "Não foi encontrado um cadastro de aluno para este usuário"));

        Inscricao inscricao = inscricaoRepository
                .findFirstByAlunoIdAndEventoIdAndStatusOrderByDataInscricaoDesc(
                        aluno.getId(),
                        evento.getId(),
                        Inscricao.StatusInscricao.ATIVA)
                .orElseThrow(() -> new ConflitoException(
                        "Você precisa ter uma inscrição ativa neste evento para confirmar presença"));

        if (presencaRepository.existsByEventoIdAndInscricaoId(
                evento.getId(),
                inscricao.getId())) {

            throw new ConflitoException(
                    "Sua presença neste evento já foi confirmada");
        }

        if (presencaRepository.existsByEventoIdAndUsuarioId(
                evento.getId(),
                usuario.id())) {

            throw new ConflitoException(
                    "Sua presença neste evento já foi confirmada");
        }

        Presenca presenca = new Presenca();

        presenca.setEvento(evento);
        presenca.setInscricao(inscricao);
        presenca.setUsuario(aluno.getUsuario());

        return presencaRepository.save(presenca);
    }

    // Confirma presença de um participante externo.
    // Não exige conta ou inscrição.
    @Transactional
    public Presenca confirmarPresencaExterno(
            String token,
            String nome,
            String email) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do participante é obrigatório");
        }

        if (nome.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "O nome deve ter no máximo 150 caracteres");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "O e-mail do participante é obrigatório");
        }

        String nomeNormalizado = nome.trim();
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        if (emailNormalizado.length() > 150
                || !emailNormalizado.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "Informe um endereço de e-mail válido");
        }

        CodigoPresenca codigo = buscarCodigoAtivo(token);
        Evento evento = codigo.getEvento();

        validarPeriodoEvento(evento);

        if (presencaRepository
                .existsByEventoIdAndEmailParticipanteExternoIgnoreCase(
                        evento.getId(),
                        emailNormalizado)) {

            throw new ConflitoException(
                    "Já existe uma presença confirmada com este e-mail neste evento");
        }

        Presenca presenca = new Presenca();

        presenca.setEvento(evento);
        presenca.setNomeParticipanteExterno(nomeNormalizado);
        presenca.setEmailParticipanteExterno(emailNormalizado);

        return presencaRepository.save(presenca);
    }

    // Localiza somente códigos ativos.
    private CodigoPresenca buscarCodigoAtivo(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "O token do QR Code é obrigatório");
        }

        return codigoPresencaRepository
                .findByTokenAndAtivoTrue(token.trim())
                .orElseThrow(() -> new ConflitoException(
                        "QR Code inválido ou inativo"));
    }

    // Confere se o evento está aprovado e dentro do período.
    private void validarPeriodoEvento(Evento evento) {

        if (evento.getStatus() != Evento.StatusEvento.APROVADO) {
            throw new ConflitoException(
                    "O evento não está aprovado para receber confirmações de presença");
        }

        if (evento.getDataInicio() == null
                || evento.getDataFim() == null) {

            throw new ConflitoException(
                    "O evento não possui um período válido");
        }

        LocalDateTime agora = LocalDateTime.now();

        if (agora.isBefore(evento.getDataInicio())) {
            throw new ConflitoException(
                    "A confirmação de presença ainda não está disponível");
        }

        if (agora.isAfter(evento.getDataFim())) {
            throw new ConflitoException(
                    "O período de confirmação de presença foi encerrado");
        }
    }

    @Transactional(readOnly = true)
    public List<DTOPresencaResponse> listarPresencasEvento(
            Long eventoId,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new ConflitoException(
                    "É necessário estar autenticado para consultar as presenças");
        }

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ConflitoException(
                        "Evento não encontrado"));

        boolean possuiAcessoAdministrativo = usuario.papeis() != null
                && (usuario.papeis().contains(Papel.CAEX)
                        || usuario.papeis().contains(Papel.ADMIN));

        boolean eCriadorDoEvento = evento.getCriadoPor() != null
                && evento.getCriadoPor().getId().equals(usuario.id());

        if (!possuiAcessoAdministrativo && !eCriadorDoEvento) {
            throw new ConflitoException(
                    "Você não tem permissão para consultar as presenças deste evento");
        }

        return presencaRepository
                .buscarListaCompletaPorEvento(eventoId)
                .stream()
                .map(DTOPresencaResponse::de)
                .toList();
    }
}
