package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;

public interface EstructuraService extends CommonService<EstructuraEntity>{

    List<EstructuraEntity> findAllFilteredByIds(List<Long> structureIds);
   
}
