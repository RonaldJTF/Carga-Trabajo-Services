package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.TareaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TareaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TareaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TareaServiceImpl implements TareaService{
    private final TareaDAO tareaDAO;

    @Override
    @Transactional(readOnly = true)
    public TareaEntity findById(Long id) throws CiadtiException {
        return tareaDAO.findById(id).orElseThrow(()-> new CiadtiException("Tarea no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaEntity> findAll() {
        return tareaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public TareaEntity save(TareaEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            tareaDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(), 
                entity.getIdEtapa(), 
                entity.getFechaInicio(), 
                entity.getFechaFin(), 
                entity.getEntregable(), 
                entity.getResponsable(), 
                entity.getActivo(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return tareaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<TareaEntity> save(Collection<TareaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = tareaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaEntity> findAllFilteredBy(TareaEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        Specification<TareaEntity> specification = new SpecificationCiadti<>(filter, orderBy);
        return tareaDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaEntity> findAllByIdEtapa(Long idEtapa) {
        return tareaDAO.findAllByIdEtapa(idEtapa);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean updateActivoById(Long id, String activo, String registradoPor) {
        return tareaDAO.updateActivoById(activo, new Date(), registradoPor, id) > 0;
    }
}
