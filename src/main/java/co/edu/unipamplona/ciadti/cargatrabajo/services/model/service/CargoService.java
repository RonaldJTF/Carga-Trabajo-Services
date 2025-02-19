package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;
import java.util.Map;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DenominacionEmpleoEntity;

public interface CargoService extends CommonService<CargoEntity>{
    List<CargoEntity> findAllBy(Map<String, Long[]> filter);
    CargoEntity findByAppointmentId(Long id);
    List<DenominacionEmpleoEntity> findAllJobTitlesByAppointmentId(Long appointmentId);
    Double getBasicMonthlyAllowance(Long validityId, Long levelId, Long salaryScaleId);
}
