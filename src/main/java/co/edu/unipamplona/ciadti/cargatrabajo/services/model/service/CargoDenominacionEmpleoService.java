package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoDenominacionEmpleoEntity;

public interface CargoDenominacionEmpleoService extends CommonService<CargoDenominacionEmpleoEntity>{
    CargoDenominacionEmpleoEntity findByIdCargoAndIdDenominacionEmpleo(Long idCargo, Long idDenominacionEmpleo);
}
