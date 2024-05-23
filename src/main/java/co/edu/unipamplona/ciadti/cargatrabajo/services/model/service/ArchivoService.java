package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ArchivoEntity;

public interface ArchivoService extends CommonService<ArchivoEntity>{

    List<ArchivoEntity> findAllByIdSeguimiento(Long idSeguimiento);

}
