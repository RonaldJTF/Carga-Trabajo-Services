package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.RolDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.RolEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.RolService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RolServiceImpl implements RolService{

    private final RolDAO rolDAO;

    @Override
    @Transactional(readOnly = true)
    public RolEntity findById(Long id) throws CiadtiException {
        return rolDAO.findById(id).orElseThrow(() -> new CiadtiException("Rol no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<RolEntity> findAll() {
        return rolDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public RolEntity save(RolEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            rolDAO.update(
                    entity.getNombre(),
                    entity.getCodigo(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return  entity;
        }
        return rolDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<RolEntity> save(Collection<RolEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = rolDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<RolEntity> findAllFilteredBy(RolEntity filter) {
        SpecificationCiadti<RolEntity> specification = new SpecificationCiadti<>(filter);
        return rolDAO.findAll(specification);
    }
}
