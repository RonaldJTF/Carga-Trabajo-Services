package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.JerarquiaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.JerarquiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.MultiPropertyComparator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.PropertyComparator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JerarquiaServiceImpl implements JerarquiaService {
    private final JerarquiaDAO jerarquiaDAO;

    @Override
    @Transactional(readOnly = true)
    public JerarquiaEntity findById(Long id) throws CiadtiException {
        return jerarquiaDAO.findById(id).orElseThrow(() -> new CiadtiException("Jerarquia no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JerarquiaEntity> findAll() {
        return jerarquiaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public JerarquiaEntity save(JerarquiaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            jerarquiaDAO.update(
                entity.getIdOrganigrama(), 
                entity.getIdDependencia(), 
                entity.getIdJerarquiaPadre(),
                entity.getOrden(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return jerarquiaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<JerarquiaEntity> save(Collection<JerarquiaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = jerarquiaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JerarquiaEntity> findAllFilteredBy(JerarquiaEntity filter) {
        SpecificationCiadti<JerarquiaEntity> specification = new SpecificationCiadti<JerarquiaEntity>(filter);
        List<JerarquiaEntity> results = jerarquiaDAO.findAll(specification);
        results = this.filter(results);
        this.orderHierarchies(results);
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public JerarquiaEntity findByIdOrganigramaAndIdDependencia(Long idOrganigrama, Long idDependencia) {
        return jerarquiaDAO.findByIdOrganigramaAndIdDependencia(idOrganigrama, idDependencia);
    }

    /**
     * Nos permite quedarnos solo con aquellas jerarquías que no son hijas en otras
     * jerarquías.
     */
    private List<JerarquiaEntity> filter(List<JerarquiaEntity> list) {
        ArrayList<JerarquiaEntity> listFiltered = new ArrayList<>();
        Integer INITIAL_COUNT = 0;
        Integer numberOfMatches;
        for (JerarquiaEntity e : list) {
            numberOfMatches = getNumberOfMatches(e, list, INITIAL_COUNT) - INITIAL_COUNT;
            if (numberOfMatches == 1) {
                listFiltered.add(e);
            }
        }
        return listFiltered;
    }

    /**
     * Nos permite saber cuantas veces se repite una jerarquía en una lista, e
     * incluso considerando las jerarquías del padre de manera recursiva.
     * Nota: Si esa jerarquía ('JerarquiaEntity') hace parte de esa misma lista
     * ('list'), entonces al menos sabrá
     * que la encontrará una vez, así que si esta 'JerarquiaEntity' figura como
     * subjerarquía en otra (s) herarquía(s),
     * este valor retornado será mayor que la unidad.
     */
    private Integer getNumberOfMatches(JerarquiaEntity JerarquiaEntity, List<JerarquiaEntity> list, Integer cont) {
        for (JerarquiaEntity obj : list) {
            if (obj.getId() == JerarquiaEntity.getId()) {
                cont += 1;
            } else {
                cont = getNumberOfMatches(JerarquiaEntity, obj.getSubJerarquias(), cont);
            }
        }
        return cont;
    }

    private void orderSubHierarchies(List<JerarquiaEntity> operationalsManagements) {
        List<PropertyComparator<JerarquiaEntity>> propertyComparators = new ArrayList<>();
        propertyComparators.add(new PropertyComparator<>("orden", true));
        propertyComparators.add(new PropertyComparator<>("id", true));
        MultiPropertyComparator<JerarquiaEntity> multiPropertyComparator = new MultiPropertyComparator<>(propertyComparators);
        Collections.sort(operationalsManagements, multiPropertyComparator);
    }

    private void orderHierarchies(List<JerarquiaEntity> operationalsManagements) {
        orderSubHierarchies(operationalsManagements);
        for (JerarquiaEntity obj : operationalsManagements) {
            if (obj.getSubJerarquias() != null && !obj.getSubJerarquias().isEmpty()) {
                orderHierarchies(obj.getSubJerarquias());
            }
        }
    }
}
