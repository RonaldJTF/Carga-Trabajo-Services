package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.MultiPropertyComparator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.PropertyComparator;
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
        List<EtapaEntity> results = etapaDAO.findAll(specification);
        results = this.filter(results);
        this.orderStages(results);
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaEntity> findAllByIdPlanTrabajo(Long idPlanTrabajo) {
        List<EtapaEntity> results = etapaDAO.findAllByIdPlanTrabajo(idPlanTrabajo);
        results = this.filter(results);
        this.orderStages(results);
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaEntity> findAllSubstages(Long id) {
        List<EtapaEntity> results = etapaDAO.findAllByIdPadre(id);
        results = this.filter(results);
        this.orderStages(results);
        return results;
    }

    /**
     * Nos permite quedarnos solo con aquellas etapas que no son hijas en otras etapas.
     */
    private List<EtapaEntity> filter(List<EtapaEntity> list) {
        ArrayList<EtapaEntity> listFiltered = new ArrayList<>();
        Integer INITIAL_COUNT = 0;
        Integer numberOfMatches;
        for (EtapaEntity e : list) {
            numberOfMatches = getNumberOfMatches(e, list, INITIAL_COUNT) - INITIAL_COUNT;
            if (numberOfMatches == 1) {
                listFiltered.add(e);
            }
        }
        return listFiltered;
    }

    /**
     * Nos permite saber cuantas veces se repite una etapa en una lista, e
     * incluso considerando las etapas
     * de esa etapa padre de manera recursiva.
     * Nota: Si esa etapa ('etapEntity') hace parte de esa misma lista
     * ('list'), entonces al menos sabrá
     * que la encontrará una vez, así que si esta 'etapEntity' figura como
     * subEtapa en otra (s) etapa (s),
     * este valor retornado será mayor que la unidad.
     */
    private Integer getNumberOfMatches(EtapaEntity etapaEntity, List<EtapaEntity> list, Integer cont) {
        for (EtapaEntity obj : list) {
            if (obj.getId() == etapaEntity.getId()) {
                cont += 1;
            } else {
                cont = getNumberOfMatches(etapaEntity, obj.getSubEtapas(), cont);
            }
        }
        return cont;
    }

    private void orderSubstages(List<EtapaEntity> stages) {
        List<PropertyComparator<EtapaEntity>> propertyComparators = new ArrayList<>();
        propertyComparators.add(new PropertyComparator<>("nombre", true));
        MultiPropertyComparator<EtapaEntity> multiPropertyComparator = new MultiPropertyComparator<>(
                propertyComparators);
        Collections.sort(stages, multiPropertyComparator);
    }


    private void orderStages(List<EtapaEntity> stages) {
        orderSubstages(stages);
        for (EtapaEntity obj : stages) {
            if (obj.getSubEtapas() != null && !obj.getSubEtapas().isEmpty()) {
                orderStages(obj.getSubEtapas());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtapaEntity> findAllFilteredByIds(List<Long> stageIds) {
        List<EtapaEntity> results = etapaDAO.findAllFilteredByIds(stageIds);
        results = this.filter(results);
        this.orderStages(results);
        return results;
    }

}
