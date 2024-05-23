package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.SeguimientoArchivoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.SeguimientoArchivoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.SeguimientoArchivoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeguimientoArchivoServiceImpl implements SeguimientoArchivoService{
    private final SeguimientoArchivoDAO seguimientoArchivoDAO;

    @Override
    @Transactional(readOnly = true)
    public SeguimientoArchivoEntity findById(Long id) throws CiadtiException {
        return seguimientoArchivoDAO.findById(id).orElseThrow(() -> new CiadtiException("SeguimientoArchivo no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeguimientoArchivoEntity> findAll() {
        return seguimientoArchivoDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public SeguimientoArchivoEntity save(SeguimientoArchivoEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            seguimientoArchivoDAO.update(
                    entity.getIdSeguimiento(),
                    entity.getIdArchivo(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return  entity;
        }
        return seguimientoArchivoDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<SeguimientoArchivoEntity> save(Collection<SeguimientoArchivoEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = seguimientoArchivoDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeguimientoArchivoEntity> findAllFilteredBy(SeguimientoArchivoEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        Specification<SeguimientoArchivoEntity> specification = new SpecificationCiadti<>(filter, orderBy);
        return seguimientoArchivoDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public SeguimientoArchivoEntity findByIdSeguimientoAndIdArchivo(Long idSeguimiento, Long idArchivo) {
        return seguimientoArchivoDAO.findByIdSeguimientoAndIdArchivo(idSeguimiento, idArchivo);
    }
}
