
package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TareaEntity;

public interface TareaService extends CommonService<TareaEntity>{

    List<TareaEntity> findAllByIdEtapa(Long idEtapa);

    boolean updateActivoById(Long id, String activo, String registradoPor);
}
