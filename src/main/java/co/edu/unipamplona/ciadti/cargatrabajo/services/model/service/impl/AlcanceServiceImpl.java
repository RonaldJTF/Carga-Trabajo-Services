package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.AlcanceDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.AlcanceEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.AlcanceService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AlcanceServiceImpl implements AlcanceService {

    private final AlcanceDAO alcanceDAO;

    @Override
    @Transactional(readOnly = true)
    public AlcanceEntity findById(Long id) throws CiadtiException {
        return alcanceDAO.findById(id).orElseThrow(() -> new CiadtiException("Accion no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlcanceEntity> findAll() {
        return alcanceDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public AlcanceEntity save(AlcanceEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            alcanceDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return alcanceDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<AlcanceEntity> save(Collection<AlcanceEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = alcanceDAO.deleteByProcedure(id, register);
        if(1 != rows){
            throw new RuntimeException("Se han afectado " + rows + " filas.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlcanceEntity> findAllFilteredBy(AlcanceEntity filter) {
        SpecificationCiadti<AlcanceEntity> specification = new SpecificationCiadti<AlcanceEntity>(filter);
        return alcanceDAO.findAll(specification);
    }
    
}
