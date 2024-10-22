package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.PeriodicidadDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PeriodicidadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PeriodicidadService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PeriodicidadServiceImpl implements PeriodicidadService{

    private final PeriodicidadDAO periodicidadDAO;

    @Override
    @Transactional(readOnly = true)
    public PeriodicidadEntity findById(Long id) throws CiadtiException {
        return periodicidadDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodicidadEntity> findAll() {
        return periodicidadDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public PeriodicidadEntity save(PeriodicidadEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            periodicidadDAO.update(
                entity.getNombre(), 
                entity.getFrecuenciaAnual(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return periodicidadDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<PeriodicidadEntity> save(Collection<PeriodicidadEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = periodicidadDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodicidadEntity> findAllFilteredBy(PeriodicidadEntity filter) {
        SpecificationCiadti<PeriodicidadEntity> specification = new SpecificationCiadti<PeriodicidadEntity>(filter);
        return periodicidadDAO.findAll(specification);
    }
    
}
