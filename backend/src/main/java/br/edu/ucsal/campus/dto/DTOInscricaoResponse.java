package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Inscricao;
import br.edu.ucsal.campus.model.Inscricao.StatusInscricao;

import java.time.LocalDateTime;

public record DTOInscricaoResponse(
        Long id,
        Long alunoId,
        String alunoNome,
        Long eventoId,
        String eventoTitulo,
        LocalDateTime dataInscricao,
        StatusInscricao status,
        LocalDateTime dataCancelamento) {

    public static DTOInscricaoResponse de(Inscricao inscricao) {
        return new DTOInscricaoResponse(
                inscricao.getId(),
                inscricao.getAluno().getId(),
                inscricao.getAluno().getUsuario().getNome(),
                inscricao.getEvento().getId(),
                inscricao.getEvento().getTitulo(),
                inscricao.getDataInscricao(),
                inscricao.getStatus(),
                inscricao.getDataCancelamento()
        );
    }
}
