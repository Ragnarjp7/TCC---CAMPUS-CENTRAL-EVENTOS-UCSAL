package br.edu.ucsal.campus.dto;

import java.time.LocalDateTime;

public record DTOEventoRequest(
        String titulo,
        String descricao,
        String local,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        String modalidade,
        Integer capacidade,
        Long categoriaId,
        Long ligaId
) {
}
