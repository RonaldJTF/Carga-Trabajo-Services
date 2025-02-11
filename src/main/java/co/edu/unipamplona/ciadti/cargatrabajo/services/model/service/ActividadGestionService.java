package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadGestionEntity;
public interface ActividadGestionService extends CommonService<ActividadGestionEntity>{
    void deleteActividadGestioOperativaByProcedure(Long id, String rigistradoPor);
    ActividadGestionEntity findByIdGestionOperativa(Long idGestionOperativa);
}
