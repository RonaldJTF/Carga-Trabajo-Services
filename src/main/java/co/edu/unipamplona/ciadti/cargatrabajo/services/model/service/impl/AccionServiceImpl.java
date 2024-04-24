package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.AccionDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.AccionEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.AccionService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AccionServiceImpl implements AccionService{
    
    private final AccionDAO accionDAO;

    @Override
    @Transactional(readOnly = true)
    public AccionEntity findById(Long id) throws CiadtiException {
        return accionDAO.findById(id).orElseThrow(() -> new CiadtiException("Accion no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<AccionEntity> findAll() {
        return accionDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public AccionEntity save(AccionEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            accionDAO.update(
                    entity.getNombre(),
                    entity.getClaseIcono(),
                    entity.getClaseEstado(),
                    entity.getPath(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return  entity;
        }
        return accionDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<AccionEntity> save(Collection<AccionEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = accionDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<AccionEntity> findAllFilteredBy(AccionEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        Specification<AccionEntity> specification = new SpecificationCiadti<>(filter, orderBy);
        return accionDAO.findAll(specification);
    }

}
