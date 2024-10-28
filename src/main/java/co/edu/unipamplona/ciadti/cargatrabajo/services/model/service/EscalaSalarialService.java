package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EscalaSalarialEntity;

public interface EscalaSalarialService extends CommonService<EscalaSalarialEntity>{
    int updateStatusByNormativityId(EscalaSalarialEntity escalaSalarialEntity);
    int countByStatusAndNormativityId(String status, Long normativityId);
}
