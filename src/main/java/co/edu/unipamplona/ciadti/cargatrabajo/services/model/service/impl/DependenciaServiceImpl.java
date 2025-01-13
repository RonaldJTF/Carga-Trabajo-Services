package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.DependenciaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DependenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.DependenciaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DependenciaServiceImpl implements DependenciaService{

    private final DependenciaDAO dependenciaDAO;

    @Override
    @Transactional(readOnly = true)
    public DependenciaEntity findById(Long id) throws CiadtiException {
        return dependenciaDAO.findById(id).orElseThrow(() -> new CiadtiException("Dependencia no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DependenciaEntity> findAll() {
        return dependenciaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public DependenciaEntity save(DependenciaEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            dependenciaDAO.update(
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getIdConvencion(),
                entity.getIcono(),
                entity.getMimetype(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return dependenciaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<DependenciaEntity> save(Collection<DependenciaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = dependenciaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DependenciaEntity> findAllFilteredBy(DependenciaEntity filter) {
        SpecificationCiadti<DependenciaEntity> specification = new SpecificationCiadti<DependenciaEntity>(filter);
        return dependenciaDAO.findAll(specification);
    }
}
