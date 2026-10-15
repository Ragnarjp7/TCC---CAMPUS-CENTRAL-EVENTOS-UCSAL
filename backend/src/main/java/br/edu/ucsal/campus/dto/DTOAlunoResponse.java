package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Aluno;
import br.edu.ucsal.campus.model.enums.SituacaoAcademica;

public record DTOAlunoResponse(
        Long id, String nome, String email, String matricula,
        Long cursoId, String cursoNome, SituacaoAcademica situacao,
        String descricao, String fotoUrl) {

    public static DTOAlunoResponse de(Aluno a) {
        return new DTOAlunoResponse(
                a.getId(),
                a.getUsuario().getNome(),
                a.getUsuario().getEmail(),
                a.getMatricula(),
                a.getCurso().getId(),
                a.getCurso().getNome(),
                a.getSituacao(),
                a.getDescricao(),
                a.getFotoUrl());
    }
}