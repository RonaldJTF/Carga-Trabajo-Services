package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;

public interface UsuarioService extends CommonService<UsuarioEntity> {
    UsuarioEntity findByUsername (String userName);
    UsuarioEntity findByIdPersona(Long idPersona);
}
