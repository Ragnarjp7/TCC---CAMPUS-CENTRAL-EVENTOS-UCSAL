package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.enums.StatusLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.repository.AlunoRepository;
import br.edu.ucsal.campus.repository.LigaRepository;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MembroLigaService {

    private final MembroLigaRepository membroLigaRepository;
    private final AlunoRepository alunoRepository;
    private final LigaRepository ligaRepository;

    public MembroLigaService(
            MembroLigaRepository membroLigaRepository,
            AlunoRepository alunoRepository,
            LigaRepository ligaRepository) {

        this.membroLigaRepository = membroLigaRepository;
        this.alunoRepository = alunoRepository;
        this.ligaRepository = ligaRepository;
    }

    @Transactional
    public MembroLiga solicitarParticipacao(
            Long ligaId,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new ConflitoException("Usuário não autenticado");
        }

        if (ligaId == null) {
            throw new IllegalArgumentException("A liga é obrigatória");
        }

        // Busca o aluno associado ao usuário autenticado
        Aluno aluno = alunoRepository.findByUsuarioId(usuario.id())
                .orElseThrow(() -> new ConflitoException(
                        "Não existe um cadastro de aluno associado a este usuário"));

        // Busca a liga
        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new IllegalArgumentException("Liga não encontrada"));

        // Somente ligas aprovadas podem receber solicitações
        if (liga.getStatus() != StatusLiga.APROVADA) {
            throw new ConflitoException(
                    "Não é possível solicitar participação em uma liga que não está aprovada");
        }

        // Impede solicitações ou vínculos duplicados
        var vinculoExistente = membroLigaRepository
                .findByAlunoIdAndLigaId(aluno.getId(), liga.getId());

        if (vinculoExistente.isPresent()) {

            MembroLiga membro = vinculoExistente.get();

            if (membro.getStatus() == StatusMembro.RECUSADO
                    || membro.getStatus() == StatusMembro.INATIVO) {

                membro.setStatus(StatusMembro.SOLICITADO);
                membro.setCargo(null);

                return membroLigaRepository.save(membro);
            }

            throw new ConflitoException(
                    "Você já possui uma solicitação ou vínculo com esta liga");
        }

        // Cria a solicitação
        MembroLiga membro = new MembroLiga();
        membro.setAluno(aluno);
        membro.setLiga(liga);
        membro.setStatus(StatusMembro.SOLICITADO);

        return membroLigaRepository.save(membro);
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

    // Aprova a solicitação de participação
    @Transactional
    public MembroLiga aprovarSolicitacao(
            Long ligaId,
            Long membroId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarMembros(ligaId, usuario);

        MembroLiga membro = membroLigaRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!membro.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Esta solicitação não pertence à liga informada");
        }

        if (membro.getStatus() != StatusMembro.SOLICITADO) {
            throw new ConflitoException(
                    "Esta solicitação não está pendente");
        }

        membro.setStatus(StatusMembro.ATIVO);

        return membroLigaRepository.save(membro);
    }

    // Recusa a solicitação de participação
    @Transactional
    public MembroLiga recusarSolicitacao(
            Long ligaId,
            Long membroId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarMembros(ligaId, usuario);

        MembroLiga membro = membroLigaRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!membro.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Esta solicitação não pertence à liga informada");
        }

        if (membro.getStatus() != StatusMembro.SOLICITADO) {
            throw new ConflitoException(
                    "Esta solicitação não está pendente");
        }

        membro.setStatus(StatusMembro.RECUSADO);

        return membroLigaRepository.save(membro);
    }

    // Desativa um membro da liga
    @Transactional
    public MembroLiga desativarMembro(
            Long ligaId,
            Long membroId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarMembros(ligaId, usuario);

        MembroLiga membro = membroLigaRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado"));

        if (!membro.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Este membro não pertence à liga informada");
        }

        if (membro.getStatus() != StatusMembro.ATIVO) {
            throw new ConflitoException(
                    "Somente membros ativos podem ser desativados");
        }

        membro.setStatus(StatusMembro.INATIVO);

        return membroLigaRepository.save(membro);
    }

    // Verifica se o usuário pode gerenciar os membros da liga
    private void validarPermissaoGerenciarMembros(
            Long ligaId,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new ConflitoException("Usuário não autenticado");
        }

        MembroLiga responsavel = membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                ligaId,
                StatusMembro.ATIVO).orElseThrow(
                        () -> new ConflitoException(
                                "Você não é membro ativo desta liga"));

        if (responsavel.getCargo() == null
                || !responsavel.getCargo().getPermissoes()
                        .contains(Permissao.GERENCIAR_MEMBROS)) {

            throw new ConflitoException(
                    "Seu cargo não possui permissão para gerenciar membros");
        }
    }

    // Lista todos os membros de uma liga
    @Transactional(readOnly = true)
    public List<MembroLiga> listarMembros(
            Long ligaId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarMembros(ligaId, usuario);

        return membroLigaRepository.findByLigaId(ligaId);
    }
}