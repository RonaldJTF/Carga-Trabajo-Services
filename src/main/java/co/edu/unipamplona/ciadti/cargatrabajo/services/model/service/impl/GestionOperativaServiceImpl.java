package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DependenciaEntity;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
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
import jakarta.persistence.Query;
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
        }else{
            gestionOperativaDAO.save(entity);
        }
        Session session = entityManager.unwrap(Session.class);
        session.evict(entity);
        return entity;
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

    @Transactional(readOnly = true)
    public ActividadEntity findActividadByIdGestionOperativa(Long idGestionOperativa) {
        return gestionOperativaDAO.findActividadByIdGestionOperativa(idGestionOperativa);
    }
    
    /**
     * Método para obtener las gestiones operativas a partir de un ID de jerarquía.
     * @param hierarchyId El ID de la jerarquía.
     * @return Lista de gestiones operativas organizadas jerárquicamente.
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<GestionOperativaEntity> findAssignedOperationalsManagements(Long hierarchyId) {
        String sql= """
            WITH RECURSIVE padres AS (
                SELECT go.*
                FROM FORTALECIMIENTO.GESTIONOPERATIVA go
                INNER JOIN FORTALECIMIENTO.JERARQUIAGESTIONOPERATIVA jgo ON jgo.geop_id = go.geop_id
                WHERE jgo.jera_id = :hierarchyId

                UNION ALL

                SELECT padre.*
                FROM FORTALECIMIENTO.GESTIONOPERATIVA padre
                INNER JOIN padres hijo ON hijo.geop_idpadre = padre.geop_id
            )
            SELECT DISTINCT(p.*), a.*, jgo.jego_id  FROM padres as p
            LEFT JOIN FORTALECIMIENTO.JERARQUIAGESTIONOPERATIVA jgo ON jgo.geop_id = p.geop_id and jgo.jera_id = :hierarchyId
            LEFT JOIN FORTALECIMIENTO.ACTIVIDADGESTIONOPERATIVA ago ON p.geop_id = ago.geop_id
            LEFT JOIN FORTALECIMIENTO.ACTIVIDAD a ON a.acti_id = ago.acti_id
        """;

        Query query = entityManager.createNativeQuery(sql, Object[].class);
        query.unwrap(NativeQuery.class)
            .addEntity("p", GestionOperativaEntity.class)
            .addScalar("jego_id", StandardBasicTypes.LONG)
            .setTupleTransformer((tuple, aliases) -> {
                GestionOperativaEntity entity = (GestionOperativaEntity) tuple[0];
                entity.setIdJerarquiaGestionOperativa((Long) tuple[1]);
                return entity;
        });
        query.setParameter("hierarchyId", hierarchyId);
        List<GestionOperativaEntity> operationalsManagements =  query.getResultList();
        return buildHierarchy(operationalsManagements);
    }

    /**
     * Método para obtener las gestiones operativas a partir de un ID de jerarquía.
     * @param hierarchyId El ID de la jerarquía.
     * @return Lista de gestiones operativas organizadas jerárquicamente.
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<GestionOperativaEntity> findAssignedOperationalsManagements(Long hierarchyId, List<Long> operationalManagementIds) {
        String sql= """
            WITH RECURSIVE padres AS (
                SELECT go.*
                FROM FORTALECIMIENTO.GESTIONOPERATIVA go
                INNER JOIN FORTALECIMIENTO.JERARQUIAGESTIONOPERATIVA jgo ON jgo.geop_id = go.geop_id
                WHERE jgo.jera_id = :hierarchyId AND go.geop_id IN (:operationalManagementIds)

                UNION ALL

                SELECT padre.*
                FROM FORTALECIMIENTO.GESTIONOPERATIVA padre
                INNER JOIN padres hijo ON hijo.geop_idpadre = padre.geop_id
            )
            SELECT DISTINCT(p.*), a.*, jgo.jego_id  FROM padres as p
            LEFT JOIN FORTALECIMIENTO.JERARQUIAGESTIONOPERATIVA jgo ON jgo.geop_id = p.geop_id and jgo.jera_id = :hierarchyId
            LEFT JOIN FORTALECIMIENTO.ACTIVIDADGESTIONOPERATIVA ago ON p.geop_id = ago.geop_id
            LEFT JOIN FORTALECIMIENTO.ACTIVIDAD a ON a.acti_id = ago.acti_id
        """;    

        Query query = entityManager.createNativeQuery(sql, Object[].class);
        query.unwrap(NativeQuery.class)
            .addEntity("p", GestionOperativaEntity.class)
            .addScalar("jego_id", StandardBasicTypes.LONG)
            .setTupleTransformer((tuple, aliases) -> {
                GestionOperativaEntity entity = (GestionOperativaEntity) tuple[0];
                entity.setIdJerarquiaGestionOperativa((Long) tuple[1]);
                return entity;
        });
        query.setParameter("hierarchyId", hierarchyId);
        query.setParameter("operationalManagementIds", operationalManagementIds);
        List<GestionOperativaEntity> operationalsManagements =  query.getResultList();
        return buildHierarchy(operationalsManagements);
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

    /**
     * Método para obtener gestiones operativas no gestionadas en un organigrama.
     * @param organizationalChartId El ID del organigrama.
     * @return Lista de gestiones operativas organizadas jerárquicamente.
     */
    @Override
    @Transactional(readOnly = true)
    public List<GestionOperativaEntity> findNoAssignedOperationalsManagements(Long organizationalChartId) {
        List<GestionOperativaEntity> operationalManagementByOrgChartList = gestionOperativaDAO.findNoAssignedOperationalsManagements(organizationalChartId);
        return buildHierarchy(operationalManagementByOrgChartList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findOperationalManagementByOrganizationChart(List<Long> organizationChartId){
        return gestionOperativaDAO.findOperationalManagementByOrganizationChart(organizationChartId);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<GestionOperativaEntity> findAssignedOperationalsManagementsByOrganizationChartId(Long organizationChartId) {
        String sql= """
               WITH RECURSIVE padres AS (
                    SELECT go.*, d.depe_id as idDependencia
                    FROM FORTALECIMIENTO.GESTIONOPERATIVA go
                    INNER JOIN FORTALECIMIENTO.JERARQUIAGESTIONOPERATIVA jgo ON jgo.geop_id = go.geop_id
                    INNER JOIN FORTALECIMIENTO.JERARQUIA j ON j.jera_id = jgo.jera_id
                    INNER JOIN FORTALECIMIENTO.DEPENDENCIA d ON d.depe_id = j.depe_id
                    WHERE j.orga_id = :organizationChartId
        
                    UNION ALL
        
                    SELECT padre.*, null::numeric(30,0)  as idDependencia
                    FROM FORTALECIMIENTO.GESTIONOPERATIVA padre
                    INNER JOIN padres hijo ON hijo.geop_idpadre = padre.geop_id
                )
                SELECT DISTINCT(p.*), a.*, d.* FROM padres as p
                LEFT JOIN FORTALECIMIENTO.DEPENDENCIA d ON d.depe_id = p.idDependencia
                LEFT JOIN FORTALECIMIENTO.ACTIVIDADGESTIONOPERATIVA ago ON p.geop_id = ago.geop_id
                LEFT JOIN FORTALECIMIENTO.ACTIVIDAD a ON a.acti_id = ago.acti_id
        """;

        Query query = entityManager.createNativeQuery(sql, Object[].class);
        query.unwrap(NativeQuery.class)
                .addEntity("p", GestionOperativaEntity.class)
                .addScalar("d", DependenciaEntity.class)
                .setTupleTransformer((tuple, aliases) -> {
                    GestionOperativaEntity entity = (GestionOperativaEntity) tuple[0];
                    entity.setDependencia((DependenciaEntity) tuple[1]);
                    return entity;
                });
        query.setParameter("hierarchyId", organizationChartId);
        List<GestionOperativaEntity> operationalsManagements =  query.getResultList();
        return buildHierarchy(operationalsManagements);
    }
}
