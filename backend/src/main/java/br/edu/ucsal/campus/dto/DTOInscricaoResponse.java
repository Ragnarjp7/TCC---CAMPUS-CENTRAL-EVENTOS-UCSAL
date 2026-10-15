package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Inscricao;
import java.time.LocalDateTime;

public record DTOInscricaoResponse(
        Long id,
        Long alunoId,
        String alunoNome,
        Long eventoId,
        String eventoTitulo,
        LocalDateTime dataInscricao) {

    public static DTOInscricaoResponse de(Inscricao i) {
        return new DTOInscricaoResponse(
                i.getId(),
                i.getAluno().getId(),
                i.getAluno().getUsuario().getNome(),
                i.getEvento().getId(),
                i.getEvento().getTitulo(),
                i.getDataInscricao());
    }
}