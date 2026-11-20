package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.MembroLiga;
import br.edu.ucsal.campus.model.enums.StatusMembro;

public record DTOMembroLigaResponse(
        Long id,
        Long alunoId,
        String alunoNome,
        String alunoEmail,
        String matricula,
        Long ligaId,
        String ligaNome,
        StatusMembro status,
        Long cargoId,
        String cargoNome) {
    public static DTOMembroLigaResponse de(MembroLiga membro) {
        return new DTOMembroLigaResponse(
                membro.getId(),
                membro.getAluno().getId(),
                membro.getAluno().getUsuario().getNome(),
                membro.getAluno().getUsuario().getEmail(),
                membro.getAluno().getMatricula(),
                membro.getLiga().getId(),
                membro.getLiga().getNome(),
                membro.getStatus(),
                membro.getCargo() != null ? membro.getCargo().getId() : null,
                membro.getCargo() != null ? membro.getCargo().getNome() : null);
    }
}