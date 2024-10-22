package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CategoriaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CategoriaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CategoriaService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class CategoriaServiceImpl implements CategoriaService{

    private final CategoriaDAO categoriaDAO;

    @Override
    @Transactional(readOnly = true)
    public CategoriaEntity findById(Long id) throws CiadtiException {
        return categoriaDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaEntity> findAll() {
        return categoriaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CategoriaEntity save(CategoriaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            categoriaDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return categoriaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<CategoriaEntity> save(Collection<CategoriaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = categoriaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<CategoriaEntity> findAllFilteredBy(CategoriaEntity filter) {
        SpecificationCiadti<CategoriaEntity> specification = new SpecificationCiadti<CategoriaEntity>(filter);
        return categoriaDAO.findAll(specification);
    }
    
}
