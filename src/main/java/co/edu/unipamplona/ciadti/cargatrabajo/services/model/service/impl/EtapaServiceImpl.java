package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.EtapaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EtapaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EtapaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EtapaServiceImpl implements EtapaService{
    private final EtapaDAO etapaDAO;

    @Override
    @Transactional(readOnly = true)
    public EtapaEntity findById(Long id) throws CiadtiException {
        return etapaDAO.findById(id).orElseThrow(() -> new CiadtiException("Etapa no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaEntity> findAll() {
        return etapaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public EtapaEntity save(EtapaEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            etapaDAO.update(
                    entity.getNombre(),
                    entity.getDescripcion(),
                    entity.getIdPadre(),
                    entity.getIdPlanTrabajo(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return  entity;
        }
        return etapaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<EtapaEntity> save(Collection<EtapaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = etapaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaEntity> findAllFilteredBy(EtapaEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        Specification<EtapaEntity> specification = new SpecificationCiadti<>(filter, orderBy);
        return etapaDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaEntity> findAllByIdPlanTrabajo(Long idPlanTrabajo) {
        return etapaDAO.findAllByIdPlanTrabajo(idPlanTrabajo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaEntity> findAllSubstages(Long id) {
        return etapaDAO.findAllByIdPadre(id);
    }
}
