package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CompensacionLabNivelVigenciaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CompensacionLabNivelVigenciaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CompensacionLabNivelVigenciaServiceImpl implements CompensacionLabNivelVigenciaService {

    private final CompensacionLabNivelVigenciaDAO compensacionLabNivelVigenciaDAO;

    @Override
    @Transactional(readOnly = true)
    public CompensacionLabNivelVigenciaEntity findById(Long id) throws CiadtiException {
        return compensacionLabNivelVigenciaDAO.findById(id).orElseThrow(() -> new CiadtiException("CompensacionLabNivelVigencia no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensacionLabNivelVigenciaEntity> findAll() {
        return compensacionLabNivelVigenciaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CompensacionLabNivelVigenciaEntity save(CompensacionLabNivelVigenciaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            compensacionLabNivelVigenciaDAO.update(
                entity.getIdNivel(), 
                entity.getIdCompensacionLaboral(), 
                entity.getId(), 
                entity.getIdVigencia(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return compensacionLabNivelVigenciaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<CompensacionLabNivelVigenciaEntity> save(Collection<CompensacionLabNivelVigenciaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = compensacionLabNivelVigenciaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<CompensacionLabNivelVigenciaEntity> findAllFilteredBy(CompensacionLabNivelVigenciaEntity filter) {
        SpecificationCiadti<CompensacionLabNivelVigenciaEntity> specification = new SpecificationCiadti<CompensacionLabNivelVigenciaEntity>(filter);
        return compensacionLabNivelVigenciaDAO.findAll(specification);
    }
    
}
