package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;
import java.util.Map;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;

public interface CargoService extends CommonService<CargoEntity>{
    List<CargoEntity> findAllBy(Map<String, Long[]> filter);

    CargoEntity findByAppointmentId(Long id);
}
