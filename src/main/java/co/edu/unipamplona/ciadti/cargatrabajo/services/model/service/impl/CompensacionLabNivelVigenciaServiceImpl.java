package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CompensacionLabNivelVigenciaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLaboralEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EscalaSalarialEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CompensacionLabNivelVigenciaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CompensacionLabNivelVigenciaServiceImpl implements CompensacionLabNivelVigenciaService {
    @PersistenceContext
    private EntityManager entityManager;

    private final CompensacionLabNivelVigenciaDAO compensacionLabNivelVigenciaDAO;

    @Override
    @Transactional(readOnly = true)
    public CompensacionLabNivelVigenciaEntity findById(Long id) throws CiadtiException {
        return compensacionLabNivelVigenciaDAO.findById(id).orElseThrow(() -> new CiadtiException("CompensacionLabNivelVigencia no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensacionLabNivelVigenciaEntity> findAll() {
        return compensacionLabNivelVigenciaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CompensacionLabNivelVigenciaEntity save(CompensacionLabNivelVigenciaEntity entity) {
        if(entity.getId() != null){
            entity.onUpdate();
            compensacionLabNivelVigenciaDAO.update(
                entity.getIdNivel(), 
                entity.getIdCompensacionLaboral(), 
                entity.getIdEscalaSalarial(), 
                entity.getIdVigencia(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return compensacionLabNivelVigenciaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<CompensacionLabNivelVigenciaEntity> save(Collection<CompensacionLabNivelVigenciaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = compensacionLabNivelVigenciaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<CompensacionLabNivelVigenciaEntity> findAllFilteredBy(CompensacionLabNivelVigenciaEntity filter) {
        SpecificationCiadti<CompensacionLabNivelVigenciaEntity> specification = new SpecificationCiadti<CompensacionLabNivelVigenciaEntity>(filter);
        return compensacionLabNivelVigenciaDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompensacionLabNivelVigenciaEntity> findAllBy(CompensacionLabNivelVigenciaEntity filter) {
       String jpql= " select clnv, v, ni, es, cl " + 
                " from CompensacionLabNivelVigenciaEntity clnv              " +
                " inner join VigenciaEntity v on (clnv.idVigencia = v.id)   " +
                " inner join NivelEntity ni on (clnv.idNivel = ni.id)       " +
                " inner join CompensacionLaboralEntity cl on (clnv.idCompensacionLaboral = cl.id)  " +
                " left outer join EscalaSalarialEntity es on (clnv.idEscalaSalarial = es.id)       " +
                " where 2 > 1  ";

        Map<String, Object> parameters = new HashMap<>();

        if (filter.getId() != null){
            jpql += "AND clnv.id = :id ";
            parameters.put("id", filter.getId());
        }

        if (filter.getIdVigencia() != null){
            jpql += "AND clnv.idVigencia = :idVigencia ";
            parameters.put("idVigencia", filter.getIdVigencia());
        }

        if (filter.getIdNivel() != null){
            jpql += "AND clnv.idNivel = :idNivel ";
            parameters.put("idNivel", filter.getIdNivel());
        }

        if (filter.getIdEscalaSalarial() != null){
            jpql += "AND clnv.idEscalaSalarial = :idEscalaSalarial ";
            parameters.put("idEscalaSalarial", filter.getIdEscalaSalarial());
        }

        if (filter.getIdCompensacionLaboral() != null){
            jpql += "AND clnv.idCompensacionLaboral = :idCompensacionLaboral ";
            parameters.put("idCompensacionLaboral", filter.getIdCompensacionLaboral());
        }

        jpql += "order by clnv.id, clnv.idVigencia asc, clnv.idNivel asc, clnv.idCompensacionLaboral asc ";

        Query query = entityManager.createQuery(jpql);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        } 

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<CompensacionLabNivelVigenciaEntity> levelCompensations = new ArrayList<>();
        CompensacionLabNivelVigenciaEntity levelCompensation;
        Long levelCompensationId = -1L;

        for (Object[] row : results) {
            if (((CompensacionLabNivelVigenciaEntity) row[0]).getId() != levelCompensationId){
                levelCompensation = (CompensacionLabNivelVigenciaEntity) row[0];
                levelCompensation.setVigencia((VigenciaEntity) row[1]);
                levelCompensation.setNivel((NivelEntity) row[2]);
                levelCompensation.setEscalaSalarial((EscalaSalarialEntity) row[3]);
                levelCompensation.setCompensacionLaboral((CompensacionLaboralEntity) row[4]);
                 
                levelCompensations.add(levelCompensation);
                levelCompensationId = ((CompensacionLabNivelVigenciaEntity) row[0]).getId();
            }
        }
        return levelCompensations;
    }
    
}
