
package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.model.enums.Papel;
import java.util.List;
import java.util.Set;

public record DTOMeResponse(
        Long id,
        String nome,
        String email,
        Set<Papel> papeis,
        List<DTOVinculoLigaResponse> ligas
) {
    public static DTOMeResponse de(Usuario usuario, List<MembroLiga> vinculos) {
        return new DTOMeResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                Set.copyOf(usuario.getPapeis()),
                vinculos.stream().map(DTOVinculoLigaResponse::de).toList()
        );
    }
}
