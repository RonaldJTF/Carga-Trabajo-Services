package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.ActividadGestionDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadGestionEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ActividadGestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActividadGestionServiceImpl implements ActividadGestionService {
    private final ActividadGestionDAO actividadGestionDAO;

    @Override
    @Transactional(readOnly = true)
    public ActividadGestionEntity findById(Long id) throws CiadtiException {
        return actividadGestionDAO.findById(id).orElseThrow(() -> new CiadtiException("ActividadGestionOperativa no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadGestionEntity> findAll() {
        return actividadGestionDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ActividadGestionEntity save(ActividadGestionEntity entity) {
        if (entity.getId() != null) {
            entity.onUpdate();
            actividadGestionDAO.update(
                    entity.getIdNivel(),
                    entity.getIdGestionOperativa(),
                    entity.getFrecuencia(),
                    entity.getTiempoMaximo(),
                    entity.getTiempoMinimo(),
                    entity.getTiempoPromedio(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return entity;
        }
        return actividadGestionDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<ActividadGestionEntity> save(Collection<ActividadGestionEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void deleteByProcedure(Long id, String register) {
        Integer rows = actividadGestionDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException("Se han afectado " + rows + " filas.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadGestionEntity> findAllFilteredBy(ActividadGestionEntity filter) {
        SpecificationCiadti<ActividadGestionEntity> specification = new SpecificationCiadti<ActividadGestionEntity>(filter);
        return actividadGestionDAO.findAll(specification);
    }
}
