package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.ValorVigenciaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ValorVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ValorVigenciaService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ValorVigenciaServiceImpl implements ValorVigenciaService{

    private final ValorVigenciaDAO valorVigenciaDAO;

    @Override
    @Transactional(readOnly = true)
    public ValorVigenciaEntity findById(Long id) throws CiadtiException {
        return valorVigenciaDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValorVigenciaEntity> findAll() {
        return valorVigenciaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ValorVigenciaEntity save(ValorVigenciaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            valorVigenciaDAO.update(
                entity.getIdVariable(), 
                entity.getIdVigencia(), 
                entity.getValor(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return valorVigenciaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<ValorVigenciaEntity> save(Collection<ValorVigenciaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = valorVigenciaDAO.deleteByProcedure(id, register);
        if(1 != rows){
            throw new RuntimeException("Se han afectado " + rows + " filas.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValorVigenciaEntity> findAllFilteredBy(ValorVigenciaEntity filter) {
        SpecificationCiadti<ValorVigenciaEntity> specification = new SpecificationCiadti<ValorVigenciaEntity>(filter);
        return valorVigenciaDAO.findAll(specification);
    }
 
}
