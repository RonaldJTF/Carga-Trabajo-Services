package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.RolEntity;

public interface RolService extends CommonService<RolEntity> {
    void deleteByProcedure(Long id, String register);
}
