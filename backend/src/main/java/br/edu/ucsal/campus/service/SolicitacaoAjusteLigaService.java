package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOSolicitacaoAjusteLigaRequest;
import br.edu.ucsal.campus.dto.DTOSolicitacaoAjusteLigaResponse;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.SolicitacaoAjusteLiga;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.model.enums.Papel;
import br.edu.ucsal.campus.model.enums.StatusAjusteLiga;
import br.edu.ucsal.campus.repository.LigaRepository;
import br.edu.ucsal.campus.repository.SolicitacaoAjusteLigaRepository;
import br.edu.ucsal.campus.repository.UsuarioRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitacaoAjusteLigaService {

    private final SolicitacaoAjusteLigaRepository solicitacaoRepository;
    private final LigaRepository ligaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public SolicitacaoAjusteLigaService(
            SolicitacaoAjusteLigaRepository solicitacaoRepository,
            LigaRepository ligaRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService) {

        this.solicitacaoRepository = solicitacaoRepository;
        this.ligaRepository = ligaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    // RF46 - CAEX ou ADMIN solicita ajustes em uma liga
    @Transactional
    public DTOSolicitacaoAjusteLigaResponse solicitarAjustes(
            Long ligaId,
            DTOSolicitacaoAjusteLigaRequest request,
            UsuarioAutenticado usuario) {

        validarPermissaoAdministrativa(usuario);

        if (ligaId == null || request == null
                || request.descricaoAjustes() == null
                || request.descricaoAjustes().isBlank()) {

            throw new IllegalArgumentException(
                    "A liga e a descrição dos ajustes são obrigatórias");
        }

        String descricao = request.descricaoAjustes().trim();

        if (descricao.length() > 10000) {
            throw new IllegalArgumentException(
                    "A descrição dos ajustes não pode ultrapassar 10.000 caracteres");
        }

        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Liga não encontrada"));

        // Impede mais de uma solicitação não concluída
        // para a mesma liga.
        boolean existeSolicitacaoEmAndamento =
                solicitacaoRepository.existsByLigaIdAndStatusNot(
                        ligaId,
                        StatusAjusteLiga.CONCLUIDA);

        if (existeSolicitacaoEmAndamento) {
            throw new ConflitoException(
                    "Esta liga já possui uma solicitação de ajustes em andamento");
        }

        Usuario solicitante = buscarUsuario(usuario);

        SolicitacaoAjusteLiga solicitacao =
                new SolicitacaoAjusteLiga();

        solicitacao.setLiga(liga);
        solicitacao.setSolicitante(solicitante);
        solicitacao.setDescricaoAjustes(descricao);
        solicitacao.setStatus(StatusAjusteLiga.PENDENTE);

        SolicitacaoAjusteLiga salva =
                solicitacaoRepository.save(solicitacao);

        auditoriaService.registrar(
                usuario,
                "SOLICITAR_AJUSTES",
                "SOLICITACAO_AJUSTE_LIGA",
                salva.getId(),
                null,
                "status=" + StatusAjusteLiga.PENDENTE,
                "Solicitação de ajustes criada para a liga "
                        + liga.getNome()
        );

        return DTOSolicitacaoAjusteLigaResponse.fromEntity(salva);
    }

    // RF46 - Responsável pela liga responde à solicitação
    @Transactional
    public DTOSolicitacaoAjusteLigaResponse responderAjustes(
            Long solicitacaoId,
            String resposta,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new AccessDeniedException(
                    "Usuário não autenticado");
        }

        if (resposta == null || resposta.isBlank()) {
            throw new IllegalArgumentException(
                    "A resposta aos ajustes é obrigatória");
        }

        String respostaTratada = resposta.trim();

        if (respostaTratada.length() > 10000) {
            throw new IllegalArgumentException(
                    "A resposta não pode ultrapassar 10.000 caracteres");
        }

        SolicitacaoAjusteLiga solicitacao =
                buscarSolicitacao(solicitacaoId);

        Liga liga = solicitacao.getLiga();

        if (liga.getAlunoResponsavel() == null
                || liga.getAlunoResponsavel().getUsuario() == null
                || !liga.getAlunoResponsavel().getUsuario()
                        .getId().equals(usuario.id())) {

            throw new AccessDeniedException(
                    "Somente o aluno responsável pela liga pode responder aos ajustes");
        }

        if (solicitacao.getStatus() != StatusAjusteLiga.PENDENTE) {
            throw new ConflitoException(
                    "Esta solicitação não está aguardando resposta");
        }

        Usuario responsavel = buscarUsuario(usuario);

        StatusAjusteLiga statusAnterior = solicitacao.getStatus();

        solicitacao.setResponsavelResposta(responsavel);
        solicitacao.setRespostaResponsavel(respostaTratada);
        solicitacao.setDataResposta(LocalDateTime.now());
        solicitacao.setStatus(StatusAjusteLiga.RESPONDIDA);

        SolicitacaoAjusteLiga salva =
                solicitacaoRepository.save(solicitacao);

        auditoriaService.registrar(
                usuario,
                "RESPONDER_AJUSTES",
                "SOLICITACAO_AJUSTE_LIGA",
                salva.getId(),
                "status=" + statusAnterior,
                "status=" + StatusAjusteLiga.RESPONDIDA,
                "Responsável respondeu aos ajustes da liga "
                        + liga.getNome()
        );

        return DTOSolicitacaoAjusteLigaResponse.fromEntity(salva);
    }

    // RF46 - CAEX ou ADMIN conclui a análise da resposta
    @Transactional
    public DTOSolicitacaoAjusteLigaResponse concluirAjustes(
            Long solicitacaoId,
            UsuarioAutenticado usuario) {

        validarPermissaoAdministrativa(usuario);

        SolicitacaoAjusteLiga solicitacao =
                buscarSolicitacao(solicitacaoId);

        if (solicitacao.getStatus() != StatusAjusteLiga.RESPONDIDA) {
            throw new ConflitoException(
                    "Somente solicitações respondidas podem ser concluídas");
        }

        StatusAjusteLiga statusAnterior = solicitacao.getStatus();

        solicitacao.setStatus(StatusAjusteLiga.CONCLUIDA);
        solicitacao.setDataConclusao(LocalDateTime.now());

        SolicitacaoAjusteLiga salva =
                solicitacaoRepository.save(solicitacao);

        auditoriaService.registrar(
                usuario,
                "CONCLUIR_AJUSTES",
                "SOLICITACAO_AJUSTE_LIGA",
                salva.getId(),
                "status=" + statusAnterior,
                "status=" + StatusAjusteLiga.CONCLUIDA,
                "Análise dos ajustes concluída para a liga "
                        + solicitacao.getLiga().getNome()
        );

        return DTOSolicitacaoAjusteLigaResponse.fromEntity(salva);
    }

    // Consulta o histórico de ajustes de uma liga
    @Transactional(readOnly = true)
    public List<DTOSolicitacaoAjusteLigaResponse> listarPorLiga(
            Long ligaId,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new AccessDeniedException(
                    "Usuário não autenticado");
        }

        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Liga não encontrada"));

        boolean administrador =
                possuiPermissaoAdministrativa(usuario);

        boolean responsavel = liga.getAlunoResponsavel() != null
                && liga.getAlunoResponsavel().getUsuario() != null
                && liga.getAlunoResponsavel().getUsuario()
                        .getId().equals(usuario.id());

        if (!administrador && !responsavel) {
            throw new AccessDeniedException(
                    "Você não tem permissão para consultar os ajustes desta liga");
        }

        return solicitacaoRepository
                .findByLigaIdOrderByDataSolicitacaoDesc(ligaId)
                .stream()
                .map(DTOSolicitacaoAjusteLigaResponse::fromEntity)
                .toList();
    }

    // Consulta solicitações que aguardam análise do CAEX
    @Transactional(readOnly = true)
    public List<DTOSolicitacaoAjusteLigaResponse> listarAguardandoAnalise(
            UsuarioAutenticado usuario) {

        validarPermissaoAdministrativa(usuario);

        return solicitacaoRepository
                .findByStatusOrderByDataSolicitacaoAsc(
                        StatusAjusteLiga.RESPONDIDA)
                .stream()
                .map(DTOSolicitacaoAjusteLigaResponse::fromEntity)
                .toList();
    }

    private SolicitacaoAjusteLiga buscarSolicitacao(
            Long solicitacaoId) {

        if (solicitacaoId == null) {
            throw new IllegalArgumentException(
                    "O identificador da solicitação é obrigatório");
        }

        return solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitação de ajuste não encontrada"));
    }

    private Usuario buscarUsuario(UsuarioAutenticado usuario) {

        return usuarioRepository.findById(usuario.id())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado"));
    }

    private boolean possuiPermissaoAdministrativa(
            UsuarioAutenticado usuario) {

        return usuario != null
                && usuario.papeis() != null
                && (usuario.papeis().contains(Papel.CAEX)
                || usuario.papeis().contains(Papel.ADMIN));
    }

    private void validarPermissaoAdministrativa(
            UsuarioAutenticado usuario) {

        if (!possuiPermissaoAdministrativa(usuario)) {
            throw new AccessDeniedException(
                    "Apenas usuários do CAEX ou administradores podem executar esta operação");
        }
    }
}
