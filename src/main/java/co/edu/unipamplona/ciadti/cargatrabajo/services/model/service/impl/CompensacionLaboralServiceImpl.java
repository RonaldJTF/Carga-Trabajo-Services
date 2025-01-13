package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CompensacionLaboralDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLaboralEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CompensacionLaboralService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CompensacionLaboralServiceImpl implements CompensacionLaboralService{

    private final CompensacionLaboralDAO compensacionLaboralDAO;

    @Override
    @Transactional(readOnly = true)
    public CompensacionLaboralEntity findById(Long id) throws CiadtiException {
        return compensacionLaboralDAO.findById(id).orElseThrow(() -> new CiadtiException("CompensacionLaboral no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensacionLaboralEntity> findAll() {
        return compensacionLaboralDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CompensacionLaboralEntity save(CompensacionLaboralEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            compensacionLaboralDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(), 
                entity.getEstado(), 
                entity.getIdCategoria(), 
                entity.getIdPeriodicidad(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return compensacionLaboralDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<CompensacionLaboralEntity> save(Collection<CompensacionLaboralEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = compensacionLaboralDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<CompensacionLaboralEntity> findAllFilteredBy(CompensacionLaboralEntity filter) {
        SpecificationCiadti<CompensacionLaboralEntity> specification = new SpecificationCiadti<CompensacionLaboralEntity>(filter);
        return compensacionLaboralDAO.findAll(specification);
    }
    
}
