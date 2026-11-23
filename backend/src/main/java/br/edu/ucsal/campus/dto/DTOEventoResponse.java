package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Evento;

import java.time.LocalDate;

// Evento exibido nas consultas, listas, filtros e detalhes.
public record DTOEventoResponse(
        Long id,
        String titulo,
        String descricao,
        String local,
        LocalDate dataInicio,
        LocalDate dataFim,
        String modalidade,
        Evento.StatusEvento status,
        Integer capacidade,
        long inscritos,
        Long categoriaId,
        String categoriaNome,
        Long ligaId,
        String ligaNome,
        Boolean inscrito
) {

    public static DTOEventoResponse de(
            Evento e,
            long inscritos,
            Boolean inscrito
    ) {
        boolean temLiga = e.getLiga() != null;
        boolean temCategoria = e.getCategoria() != null;

        return new DTOEventoResponse(
                e.getId(),
                e.getTitulo(),
                e.getDescricao(),
                e.getLocal(),
                e.getDataInicio(),
                e.getDataFim(),
                e.getModalidade(),
                e.getStatus(),
                e.getCapacidade(),
                inscritos,
                temCategoria ? e.getCategoria().getId() : null,
                temCategoria ? e.getCategoria().getNome() : null,
                temLiga ? e.getLiga().getId() : null,
                temLiga ? e.getLiga().getNome() : null,
                inscrito
        );
    }
}
