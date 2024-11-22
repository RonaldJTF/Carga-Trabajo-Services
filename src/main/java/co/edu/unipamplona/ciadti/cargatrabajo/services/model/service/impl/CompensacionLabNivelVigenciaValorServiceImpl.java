package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CompensacionLabNivelVigValorDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigValorEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CompensacionLabNivelVigValorService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CompensacionLabNivelVigenciaValorServiceImpl implements CompensacionLabNivelVigValorService {
    private final CompensacionLabNivelVigValorDAO compensacionLabNivelVigValorDAO;

    @Override
    @Transactional(readOnly = true)
    public CompensacionLabNivelVigValorEntity findById(Long id) throws CiadtiException {
        return compensacionLabNivelVigValorDAO.findById(id).orElseThrow(() -> new CiadtiException("CompensacionLabNivelVigValor no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensacionLabNivelVigValorEntity> findAll() {
        return compensacionLabNivelVigValorDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CompensacionLabNivelVigValorEntity save(CompensacionLabNivelVigValorEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            compensacionLabNivelVigValorDAO.update(
                entity.getIdCompensacionLabNivelVigencia(), 
                entity.getIdRegla(), 
                entity.getIdVariable(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return compensacionLabNivelVigValorDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<CompensacionLabNivelVigValorEntity> save(Collection<CompensacionLabNivelVigValorEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = compensacionLabNivelVigValorDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensacionLabNivelVigValorEntity> findAllFilteredBy(CompensacionLabNivelVigValorEntity filter) {
        SpecificationCiadti<CompensacionLabNivelVigValorEntity> specification = new SpecificationCiadti<CompensacionLabNivelVigValorEntity>(filter);
        return compensacionLabNivelVigValorDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getValueInValidityOfValueByRule(Long valueByRuleId) {
        return compensacionLabNivelVigValorDAO.getValueInValidityOfValueByRule(valueByRuleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensacionLabNivelVigValorEntity> findValuesByRulesOfLevelCompensation(Long levelCompensationId) {
       return compensacionLabNivelVigValorDAO.findValuesByRulesOfLevelCompensation(levelCompensationId);
    }
}
