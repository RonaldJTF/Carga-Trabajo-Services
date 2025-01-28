package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.DenominacionEmpleoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DenominacionEmpleoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.DenominacionEmpleoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DenominacionEmpleoServiceImpl implements DenominacionEmpleoService{

    private final DenominacionEmpleoDAO denominacionEmpleoDao;

    @Override
    @Transactional(readOnly = true)
    public DenominacionEmpleoEntity findById(Long id) throws CiadtiException {
        return denominacionEmpleoDao.findById(id).orElseThrow(() -> new CiadtiException("DenominacionEmpleo no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenominacionEmpleoEntity> findAll() {
        return denominacionEmpleoDao.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public DenominacionEmpleoEntity save(DenominacionEmpleoEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            denominacionEmpleoDao.update(
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return denominacionEmpleoDao.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<DenominacionEmpleoEntity> save(Collection<DenominacionEmpleoEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = denominacionEmpleoDao.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenominacionEmpleoEntity> findAllFilteredBy(DenominacionEmpleoEntity filter) {
        SpecificationCiadti<DenominacionEmpleoEntity> specification = new SpecificationCiadti<DenominacionEmpleoEntity>(filter);
        return denominacionEmpleoDao.findAll(specification);
    }
}
