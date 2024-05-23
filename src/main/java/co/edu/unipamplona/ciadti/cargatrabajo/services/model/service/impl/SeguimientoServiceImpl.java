package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.SeguimientoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.SeguimientoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.SeguimientoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeguimientoServiceImpl implements SeguimientoService{
    private final SeguimientoDAO seguimientoDAO;

    @Override
    @Transactional(readOnly = true)
    public SeguimientoEntity findById(Long id) throws CiadtiException {
        return seguimientoDAO.findById(id).orElseThrow(() -> new CiadtiException("Seguimiento no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeguimientoEntity> findAll() {
        return seguimientoDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public SeguimientoEntity save(SeguimientoEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            seguimientoDAO.update(
                    entity.getIdTarea(),
                    entity.getPorcentajeAvance(),
                    entity.getObservacion(),
                    entity.getActivo(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return  entity;
        }
        return seguimientoDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<SeguimientoEntity> save(Collection<SeguimientoEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = seguimientoDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeguimientoEntity> findAllFilteredBy(SeguimientoEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        Specification<SeguimientoEntity> specification = new SpecificationCiadti<>(filter, orderBy);
        return seguimientoDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeguimientoEntity> findAllByIdTarea(Long idTarea) {
        return seguimientoDAO.findAllByIdTarea(idTarea);
    }
}
