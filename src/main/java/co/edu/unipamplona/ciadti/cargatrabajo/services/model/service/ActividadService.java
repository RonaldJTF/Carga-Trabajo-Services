package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;

public interface ActividadService extends CommonService<ActividadEntity>{
    ActividadEntity findByIdEstructura(Long id);

    Double getGlobalTotalTime();
}
