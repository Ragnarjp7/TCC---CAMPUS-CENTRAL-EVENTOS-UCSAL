package br.edu.ucsal.campus.dto;

import br.edu.ucsal.campus.model.Evento;
import br.edu.ucsal.campus.model.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

// RF29 - Evento como o CAEX analisa.
// Inclui informações do responsável, liga e proposta completa.
public record DTOEventoAnaliseResponse(
        Long id,
        String titulo,
        String descricao,
        String local,
        LocalDate dataInicio,
        LocalDate dataFim,
        String modalidade,
        Evento.StatusEvento status,
        Integer capacidade,
        Long categoriaId,
        String categoriaNome,
        Long ligaId,
        String ligaNome,
        Long responsavelId,
        String responsavelNome,
        String responsavelEmail,
        String observacaoCaex,
        String proposta,
        LocalDateTime criadoEm,
        LocalDateTime enviadoEm
) {

    public static DTOEventoAnaliseResponse de(Evento e) {
        boolean temLiga = e.getLiga() != null;
        boolean temCategoria = e.getCategoria() != null;

        Usuario responsavel = e.getCriadoPor();

        return new DTOEventoAnaliseResponse(
                e.getId(),
                e.getTitulo(),
                e.getDescricao(),
                e.getLocal(),
                e.getDataInicio(),
                e.getDataFim(),
                e.getModalidade(),
                e.getStatus(),
                e.getCapacidade(),
                temCategoria ? e.getCategoria().getId() : null,
                temCategoria ? e.getCategoria().getNome() : null,
                temLiga ? e.getLiga().getId() : null,
                temLiga ? e.getLiga().getNome() : null,
                responsavel != null ? responsavel.getId() : null,
                responsavel != null ? responsavel.getNome() : null,
                responsavel != null ? responsavel.getEmail() : null,
                e.getObservacaoCaex(),
                e.getProposta(),
                e.getCriadoEm(),
                e.getEnviadoEm()
        );
    }
}
