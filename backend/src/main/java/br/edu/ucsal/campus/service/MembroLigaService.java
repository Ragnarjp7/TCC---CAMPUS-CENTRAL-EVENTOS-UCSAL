package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.enums.StatusLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.TipoMembroLiga;
import br.edu.ucsal.campus.model.enums.SituacaoAcademica;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.LigaRepository;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MembroLigaService {

    private final MembroLigaRepository membroLigaRepository;
    private final AlunoRepository alunoRepository;
    private final LigaRepository ligaRepository;
    private final AuditoriaService auditoriaService;

    public MembroLigaService(
            MembroLigaRepository membroLigaRepository,
            AlunoRepository alunoRepository,
            LigaRepository ligaRepository,
            AuditoriaService auditoriaService) {

        this.membroLigaRepository = membroLigaRepository;
        this.alunoRepository = alunoRepository;
        this.ligaRepository = ligaRepository;
        this.auditoriaService = auditoriaService;
    }

    // RF19 - Solicita participação em uma liga
    @Transactional
    public MembroLiga solicitarParticipacao(
            Long ligaId,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        if (ligaId == null) {
            throw new IllegalArgumentException("A liga é obrigatória");
        }

        Aluno aluno = alunoRepository.findByUsuarioId(usuario.id())
                .orElseThrow(() -> new ConflitoException(
                        "Não existe um cadastro de aluno associado a este usuário"));

        if (aluno.getSituacao() != SituacaoAcademica.EM_ANDAMENTO) {
            throw new ConflitoException(
                    "Somente alunos com situação acadêmica em andamento podem participar de uma liga");
        }

        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Liga não encontrada"));

        if (liga.getStatus() != StatusLiga.APROVADA) {
            throw new ConflitoException(
                    "Não é possível solicitar participação em uma liga que não está aprovada");
        }

        var vinculoExistente = membroLigaRepository
                .findByAlunoIdAndLigaId(aluno.getId(), liga.getId());

        if (vinculoExistente.isPresent()) {

            MembroLiga membro = vinculoExistente.get();
            StatusMembro statusAnterior = membro.getStatus();

            if (statusAnterior == StatusMembro.RECUSADO
                    || statusAnterior == StatusMembro.INATIVO) {

                membro.setCargo(null);
                membro.setTipoMembro(TipoMembroLiga.LIGANTE_PADRAO);
                membro.setStatus(StatusMembro.SOLICITADO);

                MembroLiga membroSalvo = membroLigaRepository.save(membro);

                auditoriaService.registrar(
                        usuario,
                        "REATIVAR_SOLICITACAO",
                        "MEMBRO_LIGA",
                        membroSalvo.getId(),
                        "status=" + statusAnterior,
                        "status=" + StatusMembro.SOLICITADO,
                        "Reenvio de solicitação de participação na liga "
                                + liga.getNome()
                );

                return membroSalvo;
            }

            throw new ConflitoException(
                    "Você já possui uma solicitação ou vínculo com esta liga");
        }

        MembroLiga membro = new MembroLiga();
        membro.setAluno(aluno);
        membro.setLiga(liga);
        membro.setCargo(null);
        membro.setTipoMembro(TipoMembroLiga.LIGANTE_PADRAO);
        membro.setStatus(StatusMembro.SOLICITADO);

        MembroLiga membroSalvo = membroLigaRepository.save(membro);

        auditoriaService.registrar(
                usuario,
                "SOLICITAR_PARTICIPACAO",
                "MEMBRO_LIGA",
                membroSalvo.getId(),
                null,
                "status=" + StatusMembro.SOLICITADO,
                "Solicitação de participação na liga " + liga.getNome()
        );

        return membroSalvo;
    }

    // Lista solicitações pendentes de uma liga
    @Transactional(readOnly = true)
    public List<MembroLiga> listarSolicitacoesPendentes(
            Long ligaId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarMembros(ligaId, usuario);

        return membroLigaRepository.findByLigaIdAndStatus(
                ligaId,
                StatusMembro.SOLICITADO);
    }

    // RF20 - Aprova a solicitação de participação
    @Transactional
    public MembroLiga aprovarSolicitacao(
            Long ligaId,
            Long membroId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarMembros(ligaId, usuario);

        MembroLiga membro = membroLigaRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitação não encontrada"));

        if (!membro.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Esta solicitação não pertence à liga informada");
        }

        if (membro.getStatus() != StatusMembro.SOLICITADO) {
            throw new ConflitoException(
                    "Esta solicitação não está pendente");
        }

        membro.setCargo(null);
        membro.setTipoMembro(TipoMembroLiga.LIGANTE_PADRAO);
        membro.setStatus(StatusMembro.ATIVO);

        MembroLiga membroSalvo = membroLigaRepository.save(membro);

        auditoriaService.registrar(
                usuario,
                "APROVAR_SOLICITACAO",
                "MEMBRO_LIGA",
                membroSalvo.getId(),
                "status=" + StatusMembro.SOLICITADO,
                "status=" + StatusMembro.ATIVO
                        + "; tipo=" + TipoMembroLiga.LIGANTE_PADRAO,
                "Aprovação da participação do aluno na liga "
                        + membro.getLiga().getNome()
        );

        return membroSalvo;
    }

    // Recusa a solicitação de participação
    @Transactional
    public MembroLiga recusarSolicitacao(
            Long ligaId,
            Long membroId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarMembros(ligaId, usuario);

        MembroLiga membro = membroLigaRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitação não encontrada"));

        if (!membro.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Esta solicitação não pertence à liga informada");
        }

        if (membro.getStatus() != StatusMembro.SOLICITADO) {
            throw new ConflitoException(
                    "Esta solicitação não está pendente");
        }

        membro.setStatus(StatusMembro.RECUSADO);

        MembroLiga membroSalvo = membroLigaRepository.save(membro);

        auditoriaService.registrar(
                usuario,
                "RECUSAR_SOLICITACAO",
                "MEMBRO_LIGA",
                membroSalvo.getId(),
                "status=" + StatusMembro.SOLICITADO,
                "status=" + StatusMembro.RECUSADO,
                "Recusa da solicitação de participação na liga "
                        + membro.getLiga().getNome()
        );

        return membroSalvo;
    }

    // Desativa um membro da liga
    @Transactional
    public MembroLiga desativarMembro(
            Long ligaId,
            Long membroId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarMembros(ligaId, usuario);

        MembroLiga membro = membroLigaRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Membro não encontrado"));

        if (!membro.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Este membro não pertence à liga informada");
        }

        if (membro.getStatus() != StatusMembro.ATIVO) {
            throw new ConflitoException(
                    "Somente membros ativos podem ser desativados");
        }

        if (membro.getCargo() != null) {

            if (membro.getCargo().getPermissoes()
                    .contains(Permissao.GERENCIAR_CARGOS)) {

                long responsaveis = membroLigaRepository.contarMembrosAtivosComPermissao(
                        ligaId,
                        StatusMembro.ATIVO,
                        Permissao.GERENCIAR_CARGOS);

                if (responsaveis <= 1) {
                    throw new ConflitoException(
                            "Não é possível desativar o último responsável pelo gerenciamento de cargos da liga");
                }
            }

            if (membro.getCargo().getPermissoes()
                    .contains(Permissao.GERENCIAR_MEMBROS)) {

                long responsaveis = membroLigaRepository.contarMembrosAtivosComPermissao(
                        ligaId,
                        StatusMembro.ATIVO,
                        Permissao.GERENCIAR_MEMBROS);

                if (responsaveis <= 1) {
                    throw new ConflitoException(
                            "Não é possível desativar o último responsável pelo gerenciamento de membros da liga");
                }
            }
        }

        membro.setStatus(StatusMembro.INATIVO);

        MembroLiga membroSalvo = membroLigaRepository.save(membro);

        auditoriaService.registrar(
                usuario,
                "DESATIVAR_MEMBRO",
                "MEMBRO_LIGA",
                membroSalvo.getId(),
                "status=" + StatusMembro.ATIVO,
                "status=" + StatusMembro.INATIVO,
                "Desativação de membro da liga "
                        + membro.getLiga().getNome()
        );

        return membroSalvo;
    }

    // Verifica se o usuário pode gerenciar os membros da liga
    private void validarPermissaoGerenciarMembros(
            Long ligaId,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        MembroLiga responsavel = membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                ligaId,
                StatusMembro.ATIVO).orElseThrow(
                        () -> new AccessDeniedException(
                                "Você não é membro ativo desta liga"));

        if (responsavel.getCargo() == null
                || !responsavel.getCargo().getPermissoes()
                        .contains(Permissao.GERENCIAR_MEMBROS)) {

            throw new AccessDeniedException(
                    "Seu cargo não possui permissão para gerenciar membros");
        }
    }

    // Lista somente os membros ativos de uma liga para usuários autenticados
    @Transactional(readOnly = true)
    public List<MembroLiga> listarMembros(
            Long ligaId,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        if (ligaId == null) {
            throw new IllegalArgumentException("A liga é obrigatória");
        }

        return membroLigaRepository.findByLigaIdAndStatus(
                ligaId,
                StatusMembro.ATIVO);
    }
}
