package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;

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
    public Iterable<GeneroEntity> findAll() {
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
    public Iterable<GeneroEntity> save(Collection<GeneroEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, GeneroEntity entity) {
        Integer rows = generoDAO.deleteByProcedure(id, entity.getRegistradorDTO().getJsonAsString());
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<GeneroEntity> findAllFilteredBy(GeneroEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        SpecificationCiadti<GeneroEntity> specification = new SpecificationCiadti<GeneroEntity>(filter, orderBy);
        return generoDAO.findAll(specification);
    }

}
