package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigValorEntity;

public interface CompensacionLabNivelVigValorService extends CommonService<CompensacionLabNivelVigValorEntity>{

    Double getValueInValidityOfValueByRule(Long valueByRuleId);

    List<CompensacionLabNivelVigValorEntity> findValuesByRulesOfLevelCompensation(Long levelCompensationId);

}
