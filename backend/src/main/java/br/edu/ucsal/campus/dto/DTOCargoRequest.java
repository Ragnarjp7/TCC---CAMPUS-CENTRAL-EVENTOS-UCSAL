package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.enums.Permissao;
import java.util.Set;

public record DTOCargoRequest(
        String nome,
        Set<Permissao> permissoes
) {}
