package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.ActividadDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ActividadService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ActividadServiceImpl implements ActividadService{

    private final ActividadDAO actividadDAO;

    @Override
    @Transactional(readOnly = true)
    public ActividadEntity findById(Long id) throws CiadtiException {
        return actividadDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<ActividadEntity> findAll() {
        return actividadDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ActividadEntity save(ActividadEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            actividadDAO.update(
                entity.getIdNivel(),
                entity.getIdEstructura(),
                entity.getFrecuencia(),
                entity.getTiempoMaximo(),
                entity.getTiempoMinimo(),
                entity.getTiempoPromedio(),
                entity.getFechaCambio(),
                entity.getRegistradoPor(),
                entity.getId());
            return entity;
        }
        return actividadDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<ActividadEntity> save(Collection<ActividadEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = actividadDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadEntity> findAllFilteredBy(ActividadEntity filter) {
        SpecificationCiadti<ActividadEntity> specification = new SpecificationCiadti<ActividadEntity>(filter);
        return actividadDAO.findAll(specification);
    }
}
