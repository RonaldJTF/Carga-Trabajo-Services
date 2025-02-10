package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;

public interface NivelService extends CommonService<NivelEntity>{
    List<NivelEntity> findAllInSomeActivity();
}
