package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DependenciaEntity;

public interface DependenciaService extends CommonService<DependenciaEntity>{

    DependenciaEntity findByHierarchyId(Long hierarchyId);

}
