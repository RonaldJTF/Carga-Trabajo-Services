package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EtapaEntity;

public interface EtapaService extends CommonService<EtapaEntity> {

    List<EtapaEntity> findAllByIdPlanTrabajo(Long idPlanTrabajo);

    List<EtapaEntity> findAllSubstages(Long id);

    List<EtapaEntity> findAllFilteredByIds(List<Long> stageIds);

}
