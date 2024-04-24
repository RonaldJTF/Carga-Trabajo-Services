package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.TipologiaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TipologiaServiceImpl implements TipologiaService{
    
    private final TipologiaDAO tipologiaDAO;

    @Override
    @Transactional(readOnly = true)
    public TipologiaEntity findById(Long id) throws CiadtiException {
        return tipologiaDAO.findById(id).orElseThrow(() -> new CiadtiException("Tipologia no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<TipologiaEntity> findAll() {
        return tipologiaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public TipologiaEntity save(TipologiaEntity entity) {
        if (entity.getId() != null){
            tipologiaDAO.update(entity.getIdTipologiaSiguiente(),
                                entity.getNombre(), 
                                entity.getClaseIcono(), 
                                entity.getNombreColor(), 
                                entity.getEsDependencia(), 
                                entity.getFechaCambio(), 
                                entity.getRegistradoPor(), 
                                entity.getId());
            return entity;
        }
        return tipologiaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<TipologiaEntity> save(Collection<TipologiaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = tipologiaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<TipologiaEntity> findAllFilteredBy(TipologiaEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        SpecificationCiadti<TipologiaEntity> specificationCiadti = new SpecificationCiadti<TipologiaEntity>(filter, orderBy);
       return tipologiaDAO.findAll(specificationCiadti);
    }
}
