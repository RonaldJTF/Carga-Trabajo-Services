package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.GestionOperativaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.MultiPropertyComparator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.PropertyComparator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestionOperativaServiceImpl implements GestionOperativaService{
    @PersistenceContext
    private EntityManager entityManager;
    private final GestionOperativaDAO gestionOperativaDAO;

    @Override
    @Transactional(readOnly = true)
    public GestionOperativaEntity findById(Long id) throws CiadtiException {
        return gestionOperativaDAO.findById(id).orElseThrow(() -> new CiadtiException("GestionOperativa no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GestionOperativaEntity> findAll() {
        return gestionOperativaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public GestionOperativaEntity save(GestionOperativaEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            gestionOperativaDAO.update(
                entity.getIdPadre(),
                entity.getIdTipologia(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getOrden(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return gestionOperativaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<GestionOperativaEntity> save(Collection<GestionOperativaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = gestionOperativaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GestionOperativaEntity> findAllFilteredBy(GestionOperativaEntity filter) {
        SpecificationCiadti<GestionOperativaEntity> specification = new SpecificationCiadti<GestionOperativaEntity>(filter);
        List<GestionOperativaEntity> results = gestionOperativaDAO.findAll(specification);
        results = this.filter(results);
        this.orderOperationalsManagements(results);
        return results;
    }

    /**
     * Nos permite quedarnos solo con aquellas gestiones operativas que no son hijas en otras
     * gestiones operativas.
     */
    private List<GestionOperativaEntity> filter(List<GestionOperativaEntity> list) {
        ArrayList<GestionOperativaEntity> listFiltered = new ArrayList<>();
        Integer INITIAL_COUNT = 0;
        Integer numberOfMatches;
        for (GestionOperativaEntity e : list) {
            numberOfMatches = getNumberOfMatches(e, list, INITIAL_COUNT) - INITIAL_COUNT;
            if (numberOfMatches == 1) {
                listFiltered.add(e);
            }
        }
        return listFiltered;
    }

    /**
     * Nos permite saber cuantas veces se repite una gestión operativa en una lista, e
     * incluso considerando las gestiones operativas del padre de manera recursiva.
     * Nota: Si esa gestión operativa ('gestionOperativaEntity') hace parte de esa misma lista
     * ('list'), entonces al menos sabrá
     * que la encontrará una vez, así que si esta 'gestionOperativaEntity' figura como
     * subGestionOperativa en otra (s) gestión(es) operativas(s),
     * este valor retornado será mayor que la unidad.
     */
    private Integer getNumberOfMatches(GestionOperativaEntity gestionOperativaEntity, List<GestionOperativaEntity> list, Integer cont) {
        for (GestionOperativaEntity obj : list) {
            if (obj.getId() == gestionOperativaEntity.getId()) {
                cont += 1;
            } else {
                cont = getNumberOfMatches(gestionOperativaEntity, obj.getSubGestionesOperativas(), cont);
            }
        }
        return cont;
    }

    private void orderSubOperationalManagements(List<GestionOperativaEntity> operationalsManagements) {
        List<PropertyComparator<GestionOperativaEntity>> propertyComparators = new ArrayList<>();
        propertyComparators.add(new PropertyComparator<>("orden", true));
        propertyComparators.add(new PropertyComparator<>("id", true));
        MultiPropertyComparator<GestionOperativaEntity> multiPropertyComparator = new MultiPropertyComparator<>(propertyComparators);
        Collections.sort(operationalsManagements, multiPropertyComparator);
    }

    private void orderOperationalsManagements(List<GestionOperativaEntity> operationalsManagements) {
        orderSubOperationalManagements(operationalsManagements);
        for (GestionOperativaEntity obj : operationalsManagements) {
            if (obj.getSubGestionesOperativas() != null && !obj.getSubGestionesOperativas().isEmpty()) {
                orderOperationalsManagements(obj.getSubGestionesOperativas());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long findLastOrderByIdPadre(Long idPadre) {
        return gestionOperativaDAO.findLastOrderByIdPadre(idPadre);
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(Long idPadre, Long orden, Long id, int increment) {
        return gestionOperativaDAO.updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(idPadre, orden, id, increment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdPadreAndOrdenAndNotId(Long idPadre, Long orden, Long id) {
        return gestionOperativaDAO.existsByIdPadreAndOrdenAndNotId(idPadre, orden, id);
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(Long idPadre, Long inferiorOrder, Long superiorOrder, Long id, int increment) {
        return gestionOperativaDAO.updateOrdenByIdPadreAndOrdenBeetwenAndNotId(idPadre, inferiorOrder, superiorOrder, id, increment);
    }

    @Override
    @Transactional(readOnly = true)
    public ActividadEntity findActividadByIdGestionOperativa(Long idGestionOperativa) {
        return gestionOperativaDAO.findActividadByIdGestionOperativa(idGestionOperativa);
    }
    
    /**
     * Método para obtener la jerarquía completa de gestiones operativas a partir de un ID de jerarquía.
     * @param hierarchyId El ID de la jerarquía.
     * @return Lista de gestiones operativas organizadas jerárquicamente.
     */
    public List<GestionOperativaEntity> findOperationalManagementByHierarchy(Long hierarchyId) {
        List<GestionOperativaEntity> operationalManagementList = gestionOperativaDAO.findOperationalManagementByHierarchy(hierarchyId);
        return buildHierarchy(operationalManagementList);
    }

    /**
     * Construye la jerarquía a partir de una lista plana de gestiones operativas.
     * @param operationalManagement Lista de gestiones operativas.
     * @return Lista jerárquica de gestiones operativas.
     */
    private List<GestionOperativaEntity> buildHierarchy(List<GestionOperativaEntity> operationalManagementList) {
        Map<Long, GestionOperativaEntity> operationalManagementMap = new HashMap<>();
        List<GestionOperativaEntity> parentOperationalManagement  = new ArrayList<>();

        for (GestionOperativaEntity operationalManagementEntity : operationalManagementList) {
            operationalManagementMap.put(operationalManagementEntity.getId(), operationalManagementEntity);
            operationalManagementEntity.setSubGestionesOperativas(new ArrayList<>());
        }

        for (GestionOperativaEntity entity : operationalManagementList) {
            if (entity.getIdPadre() == null) {
                parentOperationalManagement.add(entity);
            } else {
                GestionOperativaEntity childOperationalManagement = operationalManagementMap.get(entity.getIdPadre());
                if (childOperationalManagement != null) {
                    childOperationalManagement.getSubGestionesOperativas().add(entity);
                }
            }
        }

        return parentOperationalManagement;
    }
}
