package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.enums.Permissao;

import java.util.Set;

// Vínculo ativo do usuário com uma liga; cargo nulo = ligante padrão
public record DTOVinculoLigaResponse(
        Long ligaId,
        String ligaNome,
        Long cargoId,
        String cargoNome,
        Set<Permissao> permissoes
) {
    public static DTOVinculoLigaResponse de(MembroLiga membro) {
        boolean temCargo = membro.getCargo() != null;
        return new DTOVinculoLigaResponse(
                membro.getLiga().getId(),
                membro.getLiga().getNome(),
                temCargo ? membro.getCargo().getId() : null,
                temCargo ? membro.getCargo().getNome() : null,
                temCargo ? Set.copyOf(membro.getCargo().getPermissoes()) : Set.of()
        );
    }
}
