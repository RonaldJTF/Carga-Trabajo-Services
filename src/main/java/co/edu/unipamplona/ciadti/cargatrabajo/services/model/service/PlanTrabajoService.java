package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.Map;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PlanTrabajoEntity;

public interface PlanTrabajoService extends CommonService<PlanTrabajoEntity>{
    Map<Long, Double> getAllAvances();
}
