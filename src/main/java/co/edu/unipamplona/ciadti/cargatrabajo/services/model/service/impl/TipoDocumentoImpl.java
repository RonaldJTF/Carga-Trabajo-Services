package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.TipoDocumentoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipoDocumentoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipoDocumentoService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TipoDocumentoImpl implements TipoDocumentoService{

    private final TipoDocumentoDAO tipoDocumentoDAO;

    @Override
    @Transactional(readOnly = true)
    public TipoDocumentoEntity findById(Long id) throws CiadtiException {
        return tipoDocumentoDAO.findById(id).orElseThrow(() -> new CiadtiException("TipoDocumento no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<TipoDocumentoEntity> findAll() {
        return tipoDocumentoDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public TipoDocumentoEntity save(TipoDocumentoEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            tipoDocumentoDAO.update(
                    entity.getDescripcion(),
                    entity.getAbreviatura(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return  entity;
        }
        return tipoDocumentoDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<TipoDocumentoEntity> save(Collection<TipoDocumentoEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = tipoDocumentoDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoEntity> findAllFilteredBy(TipoDocumentoEntity filter) {
        SpecificationCiadti<TipoDocumentoEntity> specification = new SpecificationCiadti<>(filter);
        return tipoDocumentoDAO.findAll(specification);
    }    
}
