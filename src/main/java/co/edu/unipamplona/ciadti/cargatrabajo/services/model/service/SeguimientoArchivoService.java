package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.SeguimientoArchivoEntity;

public interface SeguimientoArchivoService extends CommonService<SeguimientoArchivoEntity>{

    SeguimientoArchivoEntity findByIdSeguimientoAndIdArchivo(Long idSeguimiento, Long idArchivo);

}
