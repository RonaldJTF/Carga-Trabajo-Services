package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.JerarquiaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.JerarquiaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JerarquiaServiceImpl implements JerarquiaService {
    private final JerarquiaDAO jerarquiaDAO;

    @Override
    @Transactional(readOnly = true)
    public JerarquiaEntity findById(Long id) throws CiadtiException {
        return jerarquiaDAO.findById(id).orElseThrow(() -> new CiadtiException("Jerarquia no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JerarquiaEntity> findAll() {
        return jerarquiaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public JerarquiaEntity save(JerarquiaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            jerarquiaDAO.update(
                entity.getIdOrganigrama(), 
                entity.getIdDependencia(), 
                entity.getIdDependenciaPadre(),
                entity.getOrden(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return jerarquiaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<JerarquiaEntity> save(Collection<JerarquiaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void deleteByProcedure(Long id, String register) {
        Integer rows = jerarquiaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<JerarquiaEntity> findAllFilteredBy(JerarquiaEntity filter) {
        SpecificationCiadti<JerarquiaEntity> specification = new SpecificationCiadti<JerarquiaEntity>(filter);
        return jerarquiaDAO.findAll(specification);
    }
}
