package br.edu.ucsal.campus.service;

import br.edu.ucsal.campus.model.Categoria;
import br.edu.ucsal.campus.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Categoria não encontrada"));
    }

    public Categoria criar(Categoria categoria) {
        categoriaRepository.findByNomeIgnoreCase(categoria.getNome())
                .ifPresent(c -> {
                    throw new IllegalArgumentException(
                            "Já existe uma categoria com esse nome");
                });

        return categoriaRepository.save(categoria);
    }
}
