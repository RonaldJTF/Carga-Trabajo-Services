package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.ReglaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ReglaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ReglaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReglaServiceImpl implements ReglaService{

    private final ReglaDAO reglaDAO;

    @Override
    @Transactional(readOnly = true)
    public ReglaEntity findById(Long id) throws CiadtiException {
        return reglaDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglaEntity> findAll() {
        return reglaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ReglaEntity save(ReglaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            reglaDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(), 
                entity.getCondiciones(), 
                entity.getGlobal(), 
                entity.getEstado(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return reglaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<ReglaEntity> save(Collection<ReglaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = reglaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException("Se han afectado " + rows + " filas.");
        }
    }

    @Override
    public List<ReglaEntity> findAllFilteredBy(ReglaEntity filter) {
        Specification<ReglaEntity> specification = new SpecificationCiadti<ReglaEntity>(filter);
        return reglaDAO.findAll(specification);
    }

    @Override
    public List<ReglaEntity> findAllWhereVariableIsIncluded(Long idVariable) {
        return reglaDAO.findAllWhereVariableIsIncluded(idVariable);
    }

    @Override
    public List<Object[]> findAllNombresAndCondicionesAndId() throws CiadtiException {
        return reglaDAO.findAllNombresAndCondicionesAndId().orElseThrow(()-> new CiadtiException("No se encontraron reglas"));
    }
    
}
