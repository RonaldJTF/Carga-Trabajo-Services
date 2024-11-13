package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.VariableDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.VariableService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VariableServiceImpl implements VariableService{

    private final VariableDAO variableDAO;

    @Override
    @Transactional(readOnly = true)
    public VariableEntity findById(Long id) throws CiadtiException {
        return variableDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableEntity> findAll() {
        return variableDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public VariableEntity save(VariableEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            variableDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(), 
                entity.getValor(), 
                entity.getPrimaria(), 
                entity.getGlobal(), 
                entity.getPorVigencia(), 
                entity.getEstado(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(),
                entity.getId());
            return entity;
        }
        return variableDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<VariableEntity> save(Collection<VariableEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = variableDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableEntity> findAllFilteredBy(VariableEntity filter) {
        SpecificationCiadti<VariableEntity> specification = new SpecificationCiadti<VariableEntity>(filter);
        return variableDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableEntity> findAllConfigureByValidityAndActive() {
        return variableDAO.findAllByPorVigenciaAndEstado("1", "1");
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableEntity> findAllWhereIdIsIncluded(Long id) {
        return variableDAO.findAllWhereIdIsIncluded(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableEntity> findAllByIds(List<Long> variableIds) {
        return variableDAO.findAllByIds(variableIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Double findValueInValidity(Long variableId, Long validityId) {
        return variableDAO.findValueInValidity(variableId, validityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableEntity> findAllIncludedVariablesInRule(Long ruleId) {
        return variableDAO.findAllIncludedVariablesInRule(ruleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariableEntity> findAllIncludedVariablesInVariable(Long variableId) {
        return variableDAO.findAllIncludedVariablesInVariable(variableId);
    }

    @Override
    public List<VariableEntity> findByPrimariaAndGlobal() {
        return variableDAO.findByPrimariaAndGlobal();
    }
    
}
