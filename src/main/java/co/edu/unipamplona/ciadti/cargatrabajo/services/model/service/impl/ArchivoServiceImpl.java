package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.ArchivoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ArchivoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ArchivoService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ArchivoServiceImpl implements ArchivoService{
    private final ArchivoDAO archivoDAO;

    @Override
    @Transactional(readOnly = true)
    public ArchivoEntity findById(Long id) throws CiadtiException {
        return archivoDAO.findById(id).orElseThrow(() -> new CiadtiException("Archivo no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchivoEntity> findAll() {
        return archivoDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ArchivoEntity save(ArchivoEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            archivoDAO.update(
                    entity.getIdFtp(),
                    entity.getNombre(),
                    entity.getPath(),
                    entity.getTamanio(),
                    entity.getMimetype(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return  entity;
        }
        return archivoDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<ArchivoEntity> save(Collection<ArchivoEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = archivoDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchivoEntity> findAllFilteredBy(ArchivoEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        Specification<ArchivoEntity> specification = new SpecificationCiadti<>(filter, orderBy);
        return archivoDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchivoEntity> findAllByIdSeguimiento(Long idSeguimiento) {
        return archivoDAO.findAllByIdSeguimiento(idSeguimiento);
    }
}
