package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.GeneroDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GeneroEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GeneroService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GeneroServiceImpl implements GeneroService{

    private final GeneroDAO generoDAO;

    @Override
    @Transactional(readOnly = true)
    public GeneroEntity findById(Long id) throws CiadtiException {
        return generoDAO.findById(id).orElseThrow(() -> new CiadtiException("Genero no encontrado para el id :: " + id, 404)); 
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeneroEntity> findAll() {
        return generoDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public GeneroEntity save(GeneroEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            generoDAO.update(
                entity.getNombre(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return generoDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<GeneroEntity> save(Collection<GeneroEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = generoDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeneroEntity> findAllFilteredBy(GeneroEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        SpecificationCiadti<GeneroEntity> specification = new SpecificationCiadti<GeneroEntity>(filter, orderBy);
        return generoDAO.findAll(specification);
    }

}
