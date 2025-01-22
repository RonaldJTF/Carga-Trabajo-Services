package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.OrganigramaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.OrganigramaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.OrganigramaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganigramaServiceImpl implements OrganigramaService{
    private final OrganigramaDAO organigramaDAO;

    @Override
    @Transactional(readOnly = true)
    public OrganigramaEntity findById(Long id) throws CiadtiException {
        return organigramaDAO.findById(id).orElseThrow(() -> new CiadtiException("Organigrama no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganigramaEntity> findAll() {
        return organigramaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public OrganigramaEntity save(OrganigramaEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            organigramaDAO.update(
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getIdNormatividad(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return organigramaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<OrganigramaEntity> save(Collection<OrganigramaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = organigramaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganigramaEntity> findAllFilteredBy(OrganigramaEntity filter) {
        SpecificationCiadti<OrganigramaEntity> specification = new SpecificationCiadti<OrganigramaEntity>(filter);
        return organigramaDAO.findAll(specification);
    }
}
