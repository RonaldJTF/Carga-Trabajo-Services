package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.PlanTrabajoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PlanTrabajoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PlanTrabajoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanTrabajoServiceImpl implements PlanTrabajoService{
    private final PlanTrabajoDAO planTrabajoDAO;

    @Override
    @Transactional(readOnly = true)
    public PlanTrabajoEntity findById(Long id) throws CiadtiException {
        return planTrabajoDAO.findById(id).orElseThrow(() -> new CiadtiException("Plan Trabajo no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanTrabajoEntity> findAll() {
        return planTrabajoDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public PlanTrabajoEntity save(PlanTrabajoEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            planTrabajoDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(),
                entity.getFechaCambio(),
                entity.getRegistradoPor(),
                entity.getId());
            return entity;
        }
        return planTrabajoDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<PlanTrabajoEntity> save(Collection<PlanTrabajoEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = planTrabajoDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanTrabajoEntity> findAllFilteredBy(PlanTrabajoEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        Specification<PlanTrabajoEntity> specification = new SpecificationCiadti<>(filter, orderBy);
        return planTrabajoDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Double> getAllAvances() {
        List<Object[]> results = planTrabajoDAO.getAllAvances();
        Map<Long, Double> avancesMap = new HashMap<>();
        for (Object[] result : results) {
            Long planTrabajoId = ((Number) result[0]).longValue();
            Double porcentajeAvance = ((Number)(result[1] != null ? result[1] : 0)).doubleValue();
            avancesMap.put(planTrabajoId, porcentajeAvance);
        }
        return avancesMap;
    }

    @Override
    @Transactional(readOnly = true)
    public PlanTrabajoEntity findByIdStage(Long idStage) {
       return planTrabajoDAO.findByIdStage(idStage);
    }
}
