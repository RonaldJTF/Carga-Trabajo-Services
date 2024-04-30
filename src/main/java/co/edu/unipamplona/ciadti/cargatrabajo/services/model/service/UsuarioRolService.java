package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioRolEntity;

public interface UsuarioRolService extends CommonService<UsuarioRolEntity>{
    List<UsuarioRolEntity> findAllByIdUsuario(Long idUsuario);
    UsuarioRolEntity findByIdUsuarioAndIdRol(Long idUsuario, Long idRol);
}
