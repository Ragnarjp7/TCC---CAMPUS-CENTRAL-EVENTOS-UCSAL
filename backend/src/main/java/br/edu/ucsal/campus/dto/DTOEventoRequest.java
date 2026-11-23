package br.edu.ucsal.campus.dto;

import java.time.LocalDateTime;

// RF27 - Cadastro (e reenvio após ajustes) de evento.
// ligaId: obrigatório para estudantes (presidência da liga); opcional para professores e coordenadores.
// proposta: formulário completo da CAEX em JSON, guardado como veio para a análise (RF29).
public record DTOEventoRequest(
        String titulo,
        String descricao,
        String local,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        String modalidade,
        Integer capacidade,
        Long categoriaId,
        Long ligaId,
        String proposta
) {
}
