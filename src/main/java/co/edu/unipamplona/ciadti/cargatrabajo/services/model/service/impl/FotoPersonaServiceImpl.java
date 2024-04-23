package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.FotoPersonaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.FotoPersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.FotoPersonaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FotoPersonaServiceImpl implements FotoPersonaService{

    private final FotoPersonaDAO fotoPersonaDAO;

    @Override
    @Transactional(readOnly = true)
    public FotoPersonaEntity findById(Long id) throws CiadtiException {
        return fotoPersonaDAO.findById(id).orElseThrow(() -> new CiadtiException("FotoPersona no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<FotoPersonaEntity> findAll() {
        return fotoPersonaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public FotoPersonaEntity save(FotoPersonaEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            fotoPersonaDAO.update(
                entity.getIdPersona(), 
                entity.getArchivo(), 
                entity.getMimetype(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return fotoPersonaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Iterable<FotoPersonaEntity> save(Collection<FotoPersonaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, FotoPersonaEntity entity) {
        Integer rows = fotoPersonaDAO.deleteByProcedure(id, entity.getRegistradorDTO().getJsonAsString());
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<FotoPersonaEntity> findAllFilteredBy(FotoPersonaEntity filter) {
       SpecificationCiadti<FotoPersonaEntity> specification =  new SpecificationCiadti<FotoPersonaEntity>(filter);
       return fotoPersonaDAO.findAll(specification);
    }

}
