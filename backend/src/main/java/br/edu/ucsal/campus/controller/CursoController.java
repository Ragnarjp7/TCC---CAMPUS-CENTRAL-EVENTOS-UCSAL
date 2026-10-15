package br.edu.ucsal.campus.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.ucsal.campus.repository.CursoRepository;

@RestController
@RequestMapping ("/cursos")
public class CursoController {
    
    public record CursoResponse(Long id, String nome) {}

    private final CursoRepository cursoRepository;

    public CursoController(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @GetMapping 
    public List<CursoResponse> listar() {
        
        return cursoRepository.findAll().stream().map(curso -> new CursoResponse(curso.getId(), curso.getNome())).toList();
    }

}
