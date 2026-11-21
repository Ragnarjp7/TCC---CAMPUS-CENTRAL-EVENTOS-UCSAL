package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.SolicitacaoAjusteLiga;
import br.edu.ucsal.campus.model.enums.StatusAjusteLiga;

import java.time.LocalDateTime;

public record DTOSolicitacaoAjusteLigaResponse(
        Long id,
        Long ligaId,
        String ligaNome,
        Long solicitanteId,
        String solicitanteNome,
        Long responsavelRespostaId,
        String responsavelRespostaNome,
        String descricaoAjustes,
        String respostaResponsavel,
        StatusAjusteLiga status,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataResposta,
        LocalDateTime dataConclusao
) {

    public static DTOSolicitacaoAjusteLigaResponse fromEntity(
            SolicitacaoAjusteLiga solicitacao
    ) {
        return new DTOSolicitacaoAjusteLigaResponse(
                solicitacao.getId(),
                solicitacao.getLiga().getId(),
                solicitacao.getLiga().getNome(),
                solicitacao.getSolicitante().getId(),
                solicitacao.getSolicitante().getNome(),
                solicitacao.getResponsavelResposta() != null
                        ? solicitacao.getResponsavelResposta().getId()
                        : null,
                solicitacao.getResponsavelResposta() != null
                        ? solicitacao.getResponsavelResposta().getNome()
                        : null,
                solicitacao.getDescricaoAjustes(),
                solicitacao.getRespostaResponsavel(),
                solicitacao.getStatus(),
                solicitacao.getDataSolicitacao(),
                solicitacao.getDataResposta(),
                solicitacao.getDataConclusao()
        );
    }
}
