package br.edu.ucsal.campus.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// Formato estável de página para o app (evita serializar o PageImpl do Spring Data)
public record DTOPagina<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static <T> DTOPagina<T> de(Page<?> pagina, List<T> conteudo) {
        return new DTOPagina<>(
                conteudo,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isLast()
        );
    }
}
