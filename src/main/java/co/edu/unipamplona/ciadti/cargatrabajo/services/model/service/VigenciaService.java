package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VigenciaEntity;

public interface VigenciaService extends CommonService<VigenciaEntity>{
    int updateStateToAllValidities(String state);
}
