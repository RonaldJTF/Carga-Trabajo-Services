package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigenciaEntity;

public interface CompensacionLabNivelVigenciaService extends CommonService<CompensacionLabNivelVigenciaEntity>{

    List<CompensacionLabNivelVigenciaEntity> findAllBy(CompensacionLabNivelVigenciaEntity filter);
    
}
