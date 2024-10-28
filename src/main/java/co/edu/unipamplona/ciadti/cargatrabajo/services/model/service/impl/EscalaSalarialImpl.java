package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.EscalaSalarialDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EscalaSalarialEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EscalaSalarialService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class EscalaSalarialImpl implements EscalaSalarialService{

    private final EscalaSalarialDAO escalaSalarialDAO;

    @Override
    @Transactional(readOnly = true)
    public EscalaSalarialEntity findById(Long id) throws CiadtiException {
        return escalaSalarialDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscalaSalarialEntity> findAll() {
        return escalaSalarialDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public EscalaSalarialEntity save(EscalaSalarialEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            escalaSalarialDAO.update(
                entity.getNombre(), 
                entity.getCodigo(), 
                entity.getIncrementoPorcentual(), 
                entity.getIdNivel(), 
                entity.getIdNormatividad(), 
                entity.getEstado(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return escalaSalarialDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<EscalaSalarialEntity> save(Collection<EscalaSalarialEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void deleteByProcedure(Long id, String register) {
        Integer rows = escalaSalarialDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<EscalaSalarialEntity> findAllFilteredBy(EscalaSalarialEntity filter) {
        Specification<EscalaSalarialEntity> specification = new SpecificationCiadti<EscalaSalarialEntity>(filter);
        return escalaSalarialDAO.findAll(specification);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public int updateStatusByNormativityId(EscalaSalarialEntity entity) {
        entity.onUpdate();
        return escalaSalarialDAO.updateStatusByNormativityId(
            entity.getEstado(), 
            entity.getIdNormatividad(), 
            entity.getRegistradoPor(), 
            entity.getFechaCambio());
    }

    @Override
    @Transactional(readOnly = true)
    public int countByStatusAndNormativityId(String status, Long normativityId) {
        return escalaSalarialDAO.countByStatusAndNormativityId(status, normativityId);
    }
}
