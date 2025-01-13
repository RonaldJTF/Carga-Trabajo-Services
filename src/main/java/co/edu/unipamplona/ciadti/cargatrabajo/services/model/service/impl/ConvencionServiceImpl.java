package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.ConvencionDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ConvencionEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ConvencionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConvencionServiceImpl implements ConvencionService {
    private final ConvencionDAO convencionDAO;

    @Override
    @Transactional(readOnly = true)
    public ConvencionEntity findById(Long id) throws CiadtiException {
        return convencionDAO.findById(id).orElseThrow(() -> new CiadtiException("Convencion no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvencionEntity> findAll() {
        return convencionDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ConvencionEntity save(ConvencionEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            convencionDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(), 
                entity.getClaseIcono(), 
                entity.getNombreColor(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return convencionDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<ConvencionEntity> save(Collection<ConvencionEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = convencionDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<ConvencionEntity> findAllFilteredBy(ConvencionEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        SpecificationCiadti<ConvencionEntity> specification = new SpecificationCiadti<ConvencionEntity>(filter, orderBy);
        return convencionDAO.findAll(specification);
    }
}
