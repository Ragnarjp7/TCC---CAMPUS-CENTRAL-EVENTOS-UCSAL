package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Cargo;
import br.edu.ucsal.campus.model.enums.Permissao;

import java.util.Set;

public record DTOCargoResponse(
        Long id,
        String nome,
        Long ligaId,
        Set<Permissao> permissoes
) {
    public static DTOCargoResponse de(Cargo cargo) {
        return new DTOCargoResponse( 
                cargo.getId(),
                cargo.getNome(),
                cargo.getLiga().getId(),
                Set.copyOf(cargo.getPermissoes())
        );
    }
}
