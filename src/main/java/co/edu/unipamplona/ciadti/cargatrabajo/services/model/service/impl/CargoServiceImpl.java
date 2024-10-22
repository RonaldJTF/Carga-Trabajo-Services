package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CargoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CargoService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CargoServiceImpl implements CargoService{

    private final CargoDAO cargoDAO;

    @Override
    @Transactional(readOnly = true)
    public CargoEntity findById(Long id) throws CiadtiException {
        return cargoDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));

    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoEntity> findAll() {
        return cargoDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CargoEntity save(CargoEntity entity) {
        if (entity.getId() != null) {
            entity.onUpdate();
            cargoDAO.update(
                entity.getAsignacionBasica(), 
                entity.getTotalCargo(), 
                entity.getIdEstructura(), 
                entity.getIdNivel(), 
                entity.getIdNormatividad(), 
                entity.getIdEscalaSalarial(), 
                entity.getIdAlcance(), 
                entity.getIdVigencia(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return cargoDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<CargoEntity> save(Collection<CargoEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = cargoDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<CargoEntity> findAllFilteredBy(CargoEntity filter) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllFilteredBy'");
    }
    
}
