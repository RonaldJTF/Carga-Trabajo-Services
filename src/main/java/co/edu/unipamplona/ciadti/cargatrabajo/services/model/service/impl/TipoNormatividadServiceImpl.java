package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.TipoNormatividadDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipoNormatividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipoNormatividadService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class TipoNormatividadServiceImpl implements TipoNormatividadService {

    private final TipoNormatividadDAO tipoNormatividadDAO;

    @Override
    @Transactional(readOnly = true)
    public TipoNormatividadEntity findById(Long id) throws CiadtiException {
        return tipoNormatividadDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoNormatividadEntity> findAll() {
        return tipoNormatividadDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public TipoNormatividadEntity save(TipoNormatividadEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            tipoNormatividadDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return tipoNormatividadDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<TipoNormatividadEntity> save(Collection<TipoNormatividadEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = tipoNormatividadDAO.deleteByProcedure(id, register);
        if (1 != rows){
            throw new RuntimeException("Se han afectado " + rows + " filas.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoNormatividadEntity> findAllFilteredBy(TipoNormatividadEntity filter) {
        SpecificationCiadti<TipoNormatividadEntity> specification = new SpecificationCiadti<TipoNormatividadEntity>(filter);
        return tipoNormatividadDAO.findAll(specification);
    }
    
}
