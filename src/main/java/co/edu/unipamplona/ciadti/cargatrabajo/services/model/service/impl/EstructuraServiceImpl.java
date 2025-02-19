package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.*;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.ActividadDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.DependenciaDTO;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.EstructuraDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.MultiPropertyComparator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.PropertyComparator;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EstructuraServiceImpl implements EstructuraService {

    private final EstructuraDAO estructuraDAO;

    @Override
    @Transactional(readOnly = true)
    public EstructuraEntity findById(Long id) throws CiadtiException {
        return estructuraDAO.findById(id)
                .orElseThrow(() -> new CiadtiException("Estructura no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstructuraEntity> findAll() {
        return estructuraDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public EstructuraEntity save(EstructuraEntity entity) {
        if (entity.getId() != null) {
            entity.onUpdate();
            estructuraDAO.update(
                    entity.getNombre(),
                    entity.getDescripcion(),
                    entity.getIdPadre(),
                    entity.getIdTipologia(),
                    entity.getIcono(),
                    entity.getMimetype(),
                    entity.getOrden(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return entity;
        }
        return estructuraDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<EstructuraEntity> save(Collection<EstructuraEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = estructuraDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException("Se han afectado " + rows + " filas.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstructuraEntity> findAllFilteredBy(EstructuraEntity filter) {
        Specification<EstructuraEntity> specification = new SpecificationCiadti<>(filter);
        List<EstructuraEntity> results = estructuraDAO.findAll(specification);
        results = this.filter(results);
        this.orderStructures(results);
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstructuraEntity> findAllFilteredByIds(List<Long> structureIds) {
        List<EstructuraEntity> results = estructuraDAO.findAllFilteredByIds(structureIds);
        results = this.filter(results);
        this.orderStructures(results);
        return results;
    }

    /**
     * Nos permite quedarnos solo con aquellas estructuras que no son hijas en otras
     * estructuras.
     */
    private List<EstructuraEntity> filter(List<EstructuraEntity> list) {
        ArrayList<EstructuraEntity> listFiltered = new ArrayList<>();
        Integer INITIAL_COUNT = 0;
        Integer numberOfMatches;
        for (EstructuraEntity e : list) {
            numberOfMatches = getNumberOfMatches(e, list, INITIAL_COUNT) - INITIAL_COUNT;
            if (numberOfMatches == 1) {
                listFiltered.add(e);
            }
        }
        return listFiltered;
    }

    /**
     * Nos permite saber cuantas veces se repite una estructura en una lista, e
     * incluso considerando las estructuras
     * de esa estructura padre de manera recursiva.
     * Nota: Si esa estructura ('estructuraEntity') hace parte de esa misma lista
     * ('list'), entonces al menos sabrá
     * que la encontrará una vez, así que si esta 'estructuraEntity' figura como
     * subestructura en otra (s) estructura (s),
     * este valor retornado será mayor que la unidad.
     */
    private Integer getNumberOfMatches(EstructuraEntity estructuraEntity, List<EstructuraEntity> list, Integer cont) {
        for (EstructuraEntity obj : list) {
            if (obj.getId().equals(estructuraEntity.getId())) {
                cont += 1;
            } else {
                cont = getNumberOfMatches(estructuraEntity, obj.getSubEstructuras(), cont);
            }
        }
        return cont;
    }

    private void orderSubstructures(List<EstructuraEntity> structures) {
        List<PropertyComparator<EstructuraEntity>> propertyComparators = new ArrayList<>();
        propertyComparators.add(new PropertyComparator<>("orden", true));
        propertyComparators.add(new PropertyComparator<>("id", true));
        MultiPropertyComparator<EstructuraEntity> multiPropertyComparator = new MultiPropertyComparator<>(propertyComparators);
        Collections.sort(structures, multiPropertyComparator);
    }

    private void orderStructures(List<EstructuraEntity> structures) {
        orderSubstructures(structures);
        for (EstructuraEntity obj : structures) {
            if (obj.getSubEstructuras() != null && !obj.getSubEstructuras().isEmpty()) {
                orderStructures(obj.getSubEstructuras());
            }
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ActividadDTO> getTimeStatistics(Long structureId) {
        return estructuraDAO.getTimeStatistics(structureId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long findLastOrderByIdPadre(Long idPadre) {
        return estructuraDAO.findLastOrderByIdPadre(idPadre);
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(Long idPadre, Long orden, Long id, int increment) {
        return estructuraDAO.updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(idPadre, orden, id, increment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdPadreAndOrdenAndNotId(Long idPadre, Long orden, Long id) {
        return estructuraDAO.existsByIdPadreAndOrdenAndNotId(idPadre, orden, id);
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(Long idPadre, Long inferiorOrder, Long superiorOrder, Long id, int increment) {
        return estructuraDAO.updateOrdenByIdPadreAndOrdenBeetwenAndNotId(idPadre, inferiorOrder, superiorOrder, id, increment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DependenciaDTO> findAllDependencies() {
        return estructuraDAO.findAllDependencies();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstructuraEntity> findByIdPadre(Long idPadre) {
        return estructuraDAO.findByIdPadre(idPadre);
    }

    @Override
    @Transactional(readOnly = true)
    public Long findTypologyIdOfStructure(Long id) {
        return estructuraDAO.findTypologyIdOfStructure(id);
    }
}
