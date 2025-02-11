package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CargoDenominacionEmpleoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoDenominacionEmpleoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CargoDenominacionEmpleoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CargoDenominacionEmpleoServiceImpl implements CargoDenominacionEmpleoService{

    private final CargoDenominacionEmpleoDAO cargoDenominacionEmpleoDAO;

    @Override
    @Transactional(readOnly = true)
    public CargoDenominacionEmpleoEntity findById(Long id) throws CiadtiException {
        return cargoDenominacionEmpleoDAO.findById(id).orElseThrow(() -> new CiadtiException("DenominacionEmpleo no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoDenominacionEmpleoEntity> findAll() {
        return cargoDenominacionEmpleoDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CargoDenominacionEmpleoEntity save(CargoDenominacionEmpleoEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            cargoDenominacionEmpleoDAO.update(
                entity.getIdCargo(),
                entity.getIdDenominacionEmpleo(),
                entity.getTotalCargos(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return cargoDenominacionEmpleoDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<CargoDenominacionEmpleoEntity> save(Collection<CargoDenominacionEmpleoEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = cargoDenominacionEmpleoDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoDenominacionEmpleoEntity> findAllFilteredBy(CargoDenominacionEmpleoEntity filter) {
        SpecificationCiadti<CargoDenominacionEmpleoEntity> specification = new SpecificationCiadti<CargoDenominacionEmpleoEntity>(filter);
        return cargoDenominacionEmpleoDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public CargoDenominacionEmpleoEntity findByIdCargoAndIdDenominacionEmpleo(Long idCargo, Long idDenominacionEmpleo) {
        return cargoDenominacionEmpleoDAO.findByIdCargoAndIdDenominacionEmpleo(idCargo, idDenominacionEmpleo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoDenominacionEmpleoEntity> findAllByAppointmentId(Long appointmentId){
        return cargoDenominacionEmpleoDAO.findAllByIdCargo(appointmentId);
    }
}
