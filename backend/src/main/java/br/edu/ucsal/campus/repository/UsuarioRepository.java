package br.edu.ucsal.campus.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.edu.ucsal.campus.model.Usuario;
import br.edu.ucsal.campus.model.enums.Papel;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);

    // Usuários ativos com determinado papel (ex.: avisar o CAEX de um evento novo)
    @Query("SELECT u FROM Usuario u WHERE :papel MEMBER OF u.papeis AND u.ativo = true")
    List<Usuario> buscarAtivosPorPapel(@Param("papel") Papel papel);
}

