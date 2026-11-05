
package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.model.enums.Papel;
import java.util.Set;

public record DTOMeResponse(
        Long id,
        String nome,
        String email,
        Set<Papel> papeis
) {
    public static DTOMeResponse de(Usuario usuario) {
        return new DTOMeResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                Set.copyOf(usuario.getPapeis())
        );
    }
}
