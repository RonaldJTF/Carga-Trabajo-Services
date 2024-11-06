package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.VigenciaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.VigenciaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VigenciaServiceImpl implements VigenciaService{

    @PersistenceContext
    private EntityManager entityManager;

    private final VigenciaDAO vigenciaDAO;

    @Override
    @Transactional(readOnly = true)
    public VigenciaEntity findById(Long id) throws CiadtiException {
        return vigenciaDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
        
    }

    @Override
    @Transactional(readOnly = true)
    public List<VigenciaEntity> findAll() {
        return vigenciaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public VigenciaEntity save(VigenciaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            vigenciaDAO.update(
                entity.getNombre(), 
                entity.getAnio(), 
                entity.getEstado(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return vigenciaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<VigenciaEntity> save(Collection<VigenciaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = vigenciaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VigenciaEntity> findAllFilteredBy(VigenciaEntity filter) {
        OrderBy orderBy = new OrderBy("anio", false);
        SpecificationCiadti<VigenciaEntity> specification = new SpecificationCiadti<VigenciaEntity>(filter, orderBy);
        return vigenciaDAO.findAll(specification);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public int updateStateToAllValidities(String state) {
        return vigenciaDAO.updateStateToAllValidities(state);
    }    
}
