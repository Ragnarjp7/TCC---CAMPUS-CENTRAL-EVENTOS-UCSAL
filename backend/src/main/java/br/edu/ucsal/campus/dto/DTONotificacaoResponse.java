package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Notificacao;
import java.time.LocalDateTime;

public record DTONotificacaoResponse(
        Long id,
        String mensagem,
        LocalDateTime dataCriacao,
        boolean lida,
        Long eventoId
) {
    public static DTONotificacaoResponse de(Notificacao notificacao) {
        return new DTONotificacaoResponse(
                notificacao.getId(),
                notificacao.getMensagem(),
                notificacao.getDataCriacao(),
                notificacao.isLida(),
                notificacao.getEvento() != null
                        ? notificacao.getEvento().getId()
                        : null
        );
    }
}