package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ChangePasswordDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;

import java.util.Optional;

public interface UsuarioService extends CommonService<UsuarioEntity> {
    UsuarioEntity findByUsername (String userName);
    UsuarioEntity findByIdPersona(Long idPersona);
    UsuarioEntity isActivo(Long id) throws CiadtiException;
    Optional<UsuarioEntity>  findByUsernameOrEmail(String username, String correo, String activo);
    CiadtiException changePassword(ChangePasswordDTO user)throws CiadtiException;
    int updateTokenPassword(UsuarioEntity usuario);
}
