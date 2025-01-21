package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaGestionOperativaEntity;

public interface JerarquiaGestionOperativaService extends CommonService<JerarquiaGestionOperativaEntity>{
    void deleteByHierarchyIds(List<Long> hierarchyIds);
}
