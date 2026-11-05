package br.edu.ucsal.campus.security;

import br.edu.ucsal.campus.model.enums.Papel;
import java.util.Set;

public record UsuarioAutenticado(
        Long id,
        String email,
        Set<Papel> papeis
) {
}