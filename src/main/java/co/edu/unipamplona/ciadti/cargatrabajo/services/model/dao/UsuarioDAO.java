package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;
import java.util.Optional;

public interface UsuarioDAO extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByUsername (String username);
}
