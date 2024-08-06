package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

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
    public List<TipologiaAccionEntity> findAll() {
        return tipologiaAccionDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public TipologiaAccionEntity save(TipologiaAccionEntity entity) {
        System.out.println();
        System.out.println(entity.toString());
        System.out.println();
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
    public List<TipologiaAccionEntity> save(Collection<TipologiaAccionEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = tipologiaAccionDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipologiaAccionEntity> findAllFilteredBy(TipologiaAccionEntity filter) {
       SpecificationCiadti<TipologiaAccionEntity> specification = new SpecificationCiadti<TipologiaAccionEntity>(filter);
       return tipologiaAccionDAO.findAll(specification);
    }

}
