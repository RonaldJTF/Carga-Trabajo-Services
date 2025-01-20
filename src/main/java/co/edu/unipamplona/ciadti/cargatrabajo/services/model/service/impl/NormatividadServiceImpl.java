package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.NormatividadDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NormatividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NormatividadService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class NormatividadServiceImpl implements NormatividadService{

    private final NormatividadDAO normatividadDAO;

    @Override
    @Transactional(readOnly = true)
    public NormatividadEntity findById(Long id) throws CiadtiException {
        return normatividadDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NormatividadEntity> findAll() {
        return normatividadDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public NormatividadEntity save(NormatividadEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            normatividadDAO.update(
                entity.getNombre(), 
                entity.getDescripcion(), 
                entity.getEmisor(), 
                entity.getFechaInicioVigencia(), 
                entity.getFechaFinVigencia(), 
                entity.getEstado(), 
                entity.getEsEscalaSalarial(), 
                entity.getIdAlcance(), 
                entity.getIdTipoNormatividad(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return normatividadDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<NormatividadEntity> save(Collection<NormatividadEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = normatividadDAO.deleteByProcedure(id, register);
        if(1 != rows){
            throw new RuntimeException("Se han afectado " + rows + " filas.");
        }
    }

    @Override
    public List<NormatividadEntity> findAllFilteredBy(NormatividadEntity filter) {
        SpecificationCiadti<NormatividadEntity> specification = new SpecificationCiadti<NormatividadEntity>(filter);
        return normatividadDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NormatividadEntity> findGeneralNormativities(String status) {
        if(status != null){
            return normatividadDAO.findByEstadoAndEsEscalaSalarialAndIdAlcanceIsNull(status, "0");
        }else{
            return normatividadDAO.findByEsEscalaSalarialAndIdAlcanceIsNull("0");
        }
    }
    
}
