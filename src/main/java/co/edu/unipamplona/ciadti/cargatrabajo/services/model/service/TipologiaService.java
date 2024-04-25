package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;

public interface TipologiaService extends CommonService<TipologiaEntity>{

    TipologiaEntity findFirstTipology();

}
