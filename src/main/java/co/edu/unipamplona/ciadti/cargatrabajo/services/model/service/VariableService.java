package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;

public interface VariableService extends CommonService<VariableEntity>{

    List<VariableEntity> findAllConfigureByValidityAndActive();

    List<VariableEntity> findAllWhereIdIsIncluded(Long id);

    List<VariableEntity> findAllByIds(List<Long> variableIds);

    Double findValueInValidity(Long variableId, Long validityId);
    
}
