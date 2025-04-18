package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ReglaEntity;

public interface ReglaService extends CommonService<ReglaEntity>{
    List<ReglaEntity> findAllWhereVariableIsIncluded(Long idVariable);
    List<Object[]> findAllNombresAndCondicionesAndId() throws CiadtiException;
    List<ReglaEntity> getGlobalAndLevelActiveRules(Long levelId);
}
