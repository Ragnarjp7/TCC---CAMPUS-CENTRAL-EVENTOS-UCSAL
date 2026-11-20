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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Cria um novo cargo na liga
    @Transactional
    public Cargo criar(
            Long ligaId,
            DTOCargoRequest request,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarCargos(ligaId, usuario);
        validarRequest(request);

        Liga liga = ligaRepository.findById(ligaId)
                .orElseThrow(() -> new IllegalArgumentException("Liga não encontrada"));

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
                        ? new java.util.HashSet<>()
                        : new java.util.HashSet<>(request.permissoes()));

        return cargoRepository.save(cargo);
    }

    // Edita um cargo existente
    @Transactional
    public Cargo atualizar(
            Long ligaId,
            Long cargoId,
            DTOCargoRequest request,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarCargos(ligaId, usuario);
        validarRequest(request);

        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado"));

        if (!cargo.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Este cargo não pertence à liga informada");
        }

        if (cargoRepository.existsByLigaIdAndNomeIgnoreCaseAndIdNot(
                ligaId, request.nome().trim(), cargoId)) {

            throw new ConflitoException(
                    "Já existe outro cargo com esse nome nesta liga");
        }

        cargo.setNome(request.nome().trim());
        cargo.setPermissoes(
                request.permissoes() == null
                        ? new java.util.HashSet<>()
                        : new java.util.HashSet<>(request.permissoes()));

        return cargoRepository.save(cargo);
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
            throw new ConflitoException("Usuário não autenticado");
        }

        MembroLiga membro = membroLigaRepository.buscarMembroAtivo(
                usuario.id(),
                ligaId,
                StatusMembro.ATIVO).orElseThrow(
                        () -> new ConflitoException(
                                "Você não é membro ativo desta liga"));

        if (membro.getCargo() == null
                || !membro.getCargo().getPermissoes()
                        .contains(Permissao.GERENCIAR_CARGOS)) {

            throw new ConflitoException(
                    "Seu cargo não possui permissão para gerenciar cargos");
        }
    }

    // Atribui um cargo a um membro da liga
    @Transactional
    public MembroLiga atribuirCargo(
            Long ligaId,
            Long membroId,
            Long cargoId,
            UsuarioAutenticado usuario) {

        validarPermissaoGerenciarCargos(ligaId, usuario);

        MembroLiga membro = membroLigaRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado"));

        if (!membro.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Este membro não pertence à liga informada");
        }

        if (membro.getStatus() != StatusMembro.ATIVO) {
            throw new ConflitoException(
                    "Somente membros ativos podem receber cargos");
        }

        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado"));

        if (!cargo.getLiga().getId().equals(ligaId)) {
            throw new ConflitoException(
                    "Este cargo não pertence à liga informada");
        }

        membro.setCargo(cargo);

        return membroLigaRepository.save(membro);
    }
}
