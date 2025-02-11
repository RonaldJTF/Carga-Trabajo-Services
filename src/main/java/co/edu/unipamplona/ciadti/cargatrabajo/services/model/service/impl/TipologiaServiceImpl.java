package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.TipologiaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.InventarioTipologiaDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TipologiaServiceImpl implements TipologiaService{
    @PersistenceContext
    private EntityManager entityManager;

    private final TipologiaDAO tipologiaDAO;

    @Override
    @Transactional(readOnly = true)
    public TipologiaEntity findById(Long id) throws CiadtiException {
        return tipologiaDAO.findById(id).orElseThrow(() -> new CiadtiException("Tipologia no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipologiaEntity> findAll() {
        return tipologiaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public TipologiaEntity save(TipologiaEntity entity) {
        if (entity.getId() != null){
            tipologiaDAO.update(entity.getIdTipologiaSiguiente(),
                                entity.getNombre(),
                                entity.getClaseIcono(),
                                entity.getNombreColor(),
                                entity.getEsDependencia(),
                                entity.getFechaCambio(),
                                entity.getRegistradoPor(),
                                entity.getId());
            return entity;
        }
        return tipologiaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<TipologiaEntity> save(Collection<TipologiaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = tipologiaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipologiaEntity> findAllFilteredBy(TipologiaEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        SpecificationCiadti<TipologiaEntity> specificationCiadti = new SpecificationCiadti<TipologiaEntity>(filter, orderBy);
       return tipologiaDAO.findAll(specificationCiadti);
    }

    @Override
    @Transactional(readOnly = true)
    public TipologiaEntity findFirstTipology() {
        return tipologiaDAO.findFirstTipology();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipologiaEntity> findAllManagement() {
        return tipologiaDAO.findAllManagement();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioTipologiaDTO> findInventarioTipologia(){
      return tipologiaDAO.findInventarioTipologia();
    }

    @Override
    @Transactional(readOnly = true)
    public TipologiaEntity findDependencyTipology() {
        return tipologiaDAO.findDependencyTipology();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipologiaEntity> findAllWithNextTipologyFilteredBy(TipologiaEntity filter) {
        String jpql= """
           select t, ts.id, ts.idTipologiaSiguiente, ts.nombre, ts.claseIcono, ts.nombreColor, ts.esDependencia
           from TipologiaEntity t
           left join TipologiaEntity ts ON (t.idTipologiaSiguiente = ts.id)   
           where 2 > 1  
        """;

        Map<String, Object> parameters = new HashMap<>();

        if (filter.getId() != null){
            jpql += "AND t.id = :id ";
            parameters.put("id", filter.getId());
        }

        if (filter.getIdTipologiaSiguiente() != null){
            jpql += "AND t.idTipologiaSiguiente = :idTipologiaSiguiente ";
            parameters.put("idVigencia", filter.getIdTipologiaSiguiente());
        }

        if (filter.getEsDependencia() != null){
            jpql += " AND t.esDependencia = :esDependencia ";
            parameters.put("esDependencia", filter.getEsDependencia());
        }

        if (filter.getNombre() != null){
            jpql += " AND t.nombre = :nombre ";
            parameters.put("nombre", filter.getNombre());
        }

        if (filter.getClaseIcono() != null){
            jpql += " AND t.claseIcono = :claseIcono ";
            parameters.put("claseIcono", filter.getClaseIcono());
        }

        if (filter.getNombreColor() != null){
            jpql += " AND t.nombreColor = :nombreColor ";
            parameters.put("nombreColor", filter.getNombreColor());
        }

        jpql += " ORDER BY t.id DESC ";
        Query query = entityManager.createQuery(jpql);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        } 

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<TipologiaEntity> tipologies = new ArrayList<>();
        TipologiaEntity tipology;

        for (Object[] row : results) {
            tipology = (TipologiaEntity) row[0];
            tipology.setTipologiaSiguiente(
                TipologiaEntity
                .builder()
                .id((Long) row[1])
                .idTipologiaSiguiente((Long) row[2])
                .nombre((String) row[3])
                .claseIcono((String) row[4])
                .nombreColor((String) row[5])
                .esDependencia((String) row[6])
                .build()
            );
            tipologies.add(tipology);            
        }
        return tipologies;
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Encuentra una tipología con su tipología siguiente jerarquicamente, es decir, 
     * de la tipología siguiente también trae su tipología siguiente en cascada.
     * @param id: Identificador de la tipología a consultar,
     * @return: TipologiaEntity
     */
    public TipologiaEntity findWithNextTipologyHierarchicallyById(Long id) {
        String sql = """
            WITH RECURSIVE hierarchical_typology AS (
                SELECT tt.*, 1 AS level
                    FROM fortalecimiento.tipologia tt
                    WHERE tt.tipo_id = :id
                UNION ALL
                SELECT t.*, ht.level + 1
                    FROM fortalecimiento.tipologia t
                    INNER JOIN hierarchical_typology ht ON t.tipo_id  = ht.tipo_idtipologiasiguiente
            )
            SELECT * 
            FROM hierarchical_typology
            ORDER BY level;
        """;
    
        Query query = entityManager.createNativeQuery(sql, TipologiaEntity.class);
        query.setParameter("id", id);
    
        @SuppressWarnings("unchecked")
        List<TipologiaEntity> results = query.getResultList();
    
        return buildHierarchy(results);
    }
    
    private TipologiaEntity buildHierarchy(List<TipologiaEntity> list) {
        if (list.isEmpty()) {
            return null;
        }
        TipologiaEntity root = list.get(0);
        TipologiaEntity actual = root;
        for (int i = 1; i < list.size(); i++) {
            actual.setTipologiaSiguiente(list.get(i));
            actual = actual.getTipologiaSiguiente();
        }
        return root;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> findOrderOfTypologies() {
        List<Object[]> rawResults = tipologiaDAO.findOrderOfTypologies();
        Map<Long, Integer> map = new HashMap<>();
        for (Object[] row : rawResults){
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }
        return map;
    }    
}
