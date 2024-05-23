package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.FtpEntity;

public interface FtpService extends CommonService<FtpEntity>{
    FtpEntity findActive () throws CiadtiException;
}
