package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.TipologiaAccionDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaAccionEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaAccionService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TipologiaAccionServiceImpl implements TipologiaAccionService{

    private final TipologiaAccionDAO tipologiaAccionDAO;
    
    @Override
    @Transactional(readOnly = true)
    public TipologiaAccionEntity findById(Long id) throws CiadtiException {
        return tipologiaAccionDAO.findById(id).orElseThrow( () -> new CiadtiException("TipologiaAccion no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<TipologiaAccionEntity> findAll() {
        return tipologiaAccionDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public TipologiaAccionEntity save(TipologiaAccionEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            tipologiaAccionDAO.update(
                entity.getIdTipologia(), 
                entity.getIdAccion(), 
                entity.getFechaCambio(),
                entity.getRegistradoPor(),
                entity.getId());
            return entity;
        }
        return tipologiaAccionDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<TipologiaAccionEntity> save(Collection<TipologiaAccionEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, TipologiaAccionEntity entity) {
        Integer rows = tipologiaAccionDAO.deleteByProcedure(id, entity.getRegistradorDTO().getJsonAsString());
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<TipologiaAccionEntity> findAllFilteredBy(TipologiaAccionEntity filter) {
       SpecificationCiadti<TipologiaAccionEntity> specification = new SpecificationCiadti<TipologiaAccionEntity>(filter);
       return tipologiaAccionDAO.findAll(specification);
    }

}
