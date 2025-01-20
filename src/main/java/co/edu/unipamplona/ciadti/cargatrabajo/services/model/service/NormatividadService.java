package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NormatividadEntity;

public interface NormatividadService extends CommonService<NormatividadEntity>{
    List<NormatividadEntity> findGeneralNormativities(String status);
    List<NormatividadEntity> findAppointmentNormativities(String status);
}
