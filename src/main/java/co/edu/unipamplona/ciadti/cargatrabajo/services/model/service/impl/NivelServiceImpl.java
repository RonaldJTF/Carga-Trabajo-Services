package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.NivelDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NivelServiceImpl implements NivelService{

    private final NivelDAO nivelDAO;

    @Override
    @Transactional(readOnly = true)
    public NivelEntity findById(Long id) throws CiadtiException {
       return nivelDAO.findById(id).orElseThrow( () -> new CiadtiException("Nivel no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<NivelEntity> findAll() {
        return nivelDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public NivelEntity save(NivelEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            nivelDAO.update(
                entity.getDescripcion(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return nivelDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<NivelEntity> save(Collection<NivelEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = nivelDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<NivelEntity> findAllFilteredBy(NivelEntity filter) {
        SpecificationCiadti<NivelEntity> specification = new SpecificationCiadti<NivelEntity>(filter);
        return nivelDAO.findAll(specification);
    }
}
