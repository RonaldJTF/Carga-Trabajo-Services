package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.SeguimientoEntity;

public interface SeguimientoService extends CommonService<SeguimientoEntity>{

    List<SeguimientoEntity> findAllByIdTarea(Long idTarea);

}
