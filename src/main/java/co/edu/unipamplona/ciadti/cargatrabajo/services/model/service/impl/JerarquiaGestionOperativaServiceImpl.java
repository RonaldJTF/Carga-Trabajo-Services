package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.JerarquiaGestionOperativaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaGestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.JerarquiaGestionOperativaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JerarquiaGestionOperativaServiceImpl implements JerarquiaGestionOperativaService{
    private final JerarquiaGestionOperativaDAO jerarquiaGestionOperativaDAO;

    @Override
    @Transactional(readOnly = true)
    public JerarquiaGestionOperativaEntity findById(Long id) throws CiadtiException {
        return jerarquiaGestionOperativaDAO.findById(id).orElseThrow(() -> new CiadtiException("Jerarquia no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JerarquiaGestionOperativaEntity> findAll() {
        return jerarquiaGestionOperativaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public JerarquiaGestionOperativaEntity save(JerarquiaGestionOperativaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            jerarquiaGestionOperativaDAO.update(
                entity.getIdJerarquia(), 
                entity.getIdGestionOperativa(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return jerarquiaGestionOperativaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<JerarquiaGestionOperativaEntity> save(Collection<JerarquiaGestionOperativaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void deleteByProcedure(Long id, String register) {
        Integer rows = jerarquiaGestionOperativaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<JerarquiaGestionOperativaEntity> findAllFilteredBy(JerarquiaGestionOperativaEntity filter) {
        SpecificationCiadti<JerarquiaGestionOperativaEntity> specification = new SpecificationCiadti<JerarquiaGestionOperativaEntity>(filter);
        return jerarquiaGestionOperativaDAO.findAll(specification);
    }
}
