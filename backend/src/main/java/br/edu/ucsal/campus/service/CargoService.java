package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.dto.DTOCargoRequest;
import br.edu.ucsal.campus.exception.ConflitoException;
import br.edu.ucsal.campus.model.Cargo;
import br.edu.ucsal.campus.model.Liga;
import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.enums.Permissao;
import br.edu.ucsal.campus.model.enums.StatusMembro;
import br.edu.ucsal.campus.repository.CargoRepository;
import br.edu.ucsal.campus.repository.LigaRepository;
import br.edu.ucsal.campus.repository.MembroLigaRepository;
import br.edu.ucsal.campus.security.UsuarioAutenticado;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CargoService {

    private final CargoRepository cargoRepository;
    private final LigaRepository ligaRepository;
    private final MembroLigaRepository membroLigaRepository;

    public CargoService(
            CargoRepository cargoRepository,
            LigaRepository ligaRepository,
            MembroLigaRepository membroLigaRepository) {

        this.cargoRepository = cargoRepository;
        this.ligaRepository = ligaRepository;
        this.membroLigaRepository = membroLigaRepository;
    }

    // RF23 - Cria um novo cargo na liga
    @Transactional
    public Cargo criar(
            Long ligaId,
            DTOCargoRequest request,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarCargos(ligaId, usuario);
        validarRequest(request);

        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Liga não encontrada"));

        if (cargoRepository.existsByLigaIdAndNomeIgnoreCase(
                ligaId, request.nome().trim())) {

            throw new ConflitoException(
                    "Já existe um cargo com esse nome nesta liga");
        }

        Cargo cargo = new Cargo();
        cargo.setNome(request.nome().trim());
        cargo.setLiga(liga);

        cargo.setPermissoes(
                request.permissoes() == null
                        ? new HashSet<>()
                        : new HashSet<>(request.permissoes()));

        return cargoRepository.save(cargo);
    }

    // RF23 - Edita um cargo existente
    @Transactional
    public Cargo atualizar(
            Long ligaId,
            Long cargoId,
            DTOCargoRequest request,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarCargos(ligaId, usuario);
        validarRequest(request);

        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Cargo não encontrado"));

        if (!cargo.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Este cargo não pertence à liga informada");
        }

        if (cargoRepository.existsByLigaIdAndNomeIgnoreCaseAndIdNot(
                ligaId, request.nome().trim(), cargoId)) {

            throw new ConflitoException(
                    "Já existe outro cargo com esse nome nesta liga");
        }

        Set<Permissao> novasPermissoes = request.permissoes() == null
                ? new HashSet<>()
                : new HashSet<>(request.permissoes());

        boolean removendoGerenciamento =
                cargo.getPermissoes().contains(Permissao.GERENCIAR_CARGOS)
                && !novasPermissoes.contains(Permissao.GERENCIAR_CARGOS);

        if (removendoGerenciamento) {

            long membrosNoCargo =
                    membroLigaRepository.contarMembrosAtivosComPermissaoNoCargo(
                            ligaId,
                            cargoId,
                            StatusMembro.ATIVO,
                            Permissao.GERENCIAR_CARGOS);

            long responsaveisNaLiga =
                    membroLigaRepository.contarMembrosAtivosComPermissao(
                            ligaId,
                            StatusMembro.ATIVO,
                            Permissao.GERENCIAR_CARGOS);

            if (responsaveisNaLiga - membrosNoCargo < 1) {
                throw new ConflitoException(
                        "Não é possível remover a permissão GERENCIAR_CARGOS deste cargo, pois a liga ficaria sem responsável");
            }
        }

        cargo.setNome(request.nome().trim());
        cargo.setPermissoes(novasPermissoes);

        return cargoRepository.save(cargo);
    }

    // RF23 - Exclui um cargo sem vínculos com membros
    @Transactional
    public void excluir(
            Long ligaId,
            Long cargoId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarCargos(ligaId, usuario);

        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Cargo não encontrado"));

        if (!cargo.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Este cargo não pertence à liga informada");
        }

        if (membroLigaRepository.existsByCargoId(cargoId)) {
            throw new ConflitoException(
                    "Não é possível excluir este cargo, pois existem membros vinculados a ele. Remova os vínculos antes de tentar novamente");
        }

        cargoRepository.delete(cargo);
    }

    // Lista os cargos de uma liga
    @Transactional(readOnly = true)
    public List<Cargo> listar(Long ligaId) {

        if (!ligaRepository.existsById(ligaId)) {
            throw new IllegalArgumentException("Liga não encontrada");
        }

        return cargoRepository.findByLigaId(ligaId);
    }

    // RF23 - Atribui um cargo a um membro ativo
    @Transactional
    public MembroLiga atribuirCargo(
            Long ligaId,
            Long membroId,
            Long cargoId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarCargos(ligaId, usuario);

        MembroLiga membro = membroLigaRepository.findById(membroId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Membro não encontrado"));

        if (!membro.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Este membro não pertence à liga informada");
        }

        if (membro.getStatus() != StatusMembro.ATIVO) {
            throw new ConflitoException(
                    "Somente membros ativos podem receber cargos");
        }

        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Cargo não encontrado"));

        if (!cargo.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Este cargo não pertence à liga informada");
        }

        boolean possuiPermissaoAtual = membro.getCargo() != null
                && membro.getCargo().getPermissoes()
                        .contains(Permissao.GERENCIAR_CARGOS);

        boolean novoCargoPossuiPermissao = cargo.getPermissoes()
                .contains(Permissao.GERENCIAR_CARGOS);

        if (possuiPermissaoAtual && !novoCargoPossuiPermissao) {

            long responsaveisNaLiga =
                    membroLigaRepository.contarMembrosAtivosComPermissao(
                            ligaId,
                            StatusMembro.ATIVO,
                            Permissao.GERENCIAR_CARGOS);

            if (responsaveisNaLiga <= 1) {
                throw new ConflitoException(
                        "Não é possível atribuir este cargo, pois a liga ficaria sem responsável pelo gerenciamento de cargos");
            }
        }

        // setCargo também atualiza o tipo para OCUPANTE_CARGO
        membro.setCargo(cargo);

        return membroLigaRepository.save(membro);
    }

    // Valida os dados enviados
    private void validarRequest(DTOCargoRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Os dados do cargo são obrigatórios");
        }

        if (request.nome() == null || request.nome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do cargo é obrigatório");
        }

        if (request.nome().trim().length() > 80) {
            throw new IllegalArgumentException(
                    "O nome do cargo deve ter no máximo 80 caracteres");
        }
    }

    // Verifica se o usuário possui permissão para gerenciar cargos
    private void validarPermissaoGerenciarCargos(
            Long ligaId,
            UsuarioAutenticado usuario) {

        if (usuario == null) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        MembroLiga membro = membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                ligaId,
                StatusMembro.ATIVO)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Você não é membro ativo desta liga"));

        if (membro.getCargo() == null
                || !membro.getCargo().getPermissoes()
                        .contains(Permissao.GERENCIAR_CARGOS)) {

            throw new AccessDeniedException(
                    "Seu cargo não possui permissão para gerenciar cargos");
        }
    }
}
