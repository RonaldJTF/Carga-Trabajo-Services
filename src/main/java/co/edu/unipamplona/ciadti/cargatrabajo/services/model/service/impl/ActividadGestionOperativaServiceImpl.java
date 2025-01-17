package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.ActividadGestionOperativaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadGestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ActividadGestionOperativaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActividadGestionOperativaServiceImpl implements ActividadGestionOperativaService{
    private final ActividadGestionOperativaDAO actividadGestionOperativaDAO;
    
    @Override
    @Transactional(readOnly = true)
    public ActividadGestionOperativaEntity findById(Long id) throws CiadtiException {
        return actividadGestionOperativaDAO.findById(id).orElseThrow(() -> new CiadtiException("ActividadGestionOperativa no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadGestionOperativaEntity> findAll() {
        return actividadGestionOperativaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ActividadGestionOperativaEntity save(ActividadGestionOperativaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            actividadGestionOperativaDAO.update(
                entity.getIdActividad(), 
                entity.getIdGestionOperativa(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return actividadGestionOperativaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<ActividadGestionOperativaEntity> save(Collection<ActividadGestionOperativaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = actividadGestionOperativaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadGestionOperativaEntity> findAllFilteredBy(ActividadGestionOperativaEntity filter) {
        SpecificationCiadti<ActividadGestionOperativaEntity> specification = new SpecificationCiadti<ActividadGestionOperativaEntity>(filter);
        return actividadGestionOperativaDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public ActividadGestionOperativaEntity findByIdGestionOperativa(Long idGestionOperativa) {
        return actividadGestionOperativaDAO.findByIdGestionOperativa(idGestionOperativa);
    }
}
