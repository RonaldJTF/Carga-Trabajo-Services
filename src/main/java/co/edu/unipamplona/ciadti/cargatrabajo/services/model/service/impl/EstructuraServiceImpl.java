package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.*;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ActividadOutDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.ActividadDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.DependenciaDTO;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.EstructuraDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.MultiPropertyComparator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.PropertyComparator;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EstructuraServiceImpl implements EstructuraService {

    private final EstructuraDAO estructuraDAO;
    private final NivelService nivelService;

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
            if (obj.getId() == estructuraEntity.getId()) {
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

    /**
     * Método que para consultar la información de las actividades por nivel de ocupación
     * para una dependencia
     *
     * @param entity, objeto con el identificador único de la dependencia
     * @return list, objeto con información para la dependencia consultada
     */
    @Override
    @Transactional(readOnly = true)
    public List<ActividadOutDTO> statisticsDependence(EstructuraEntity entity) {
        List<ActividadDTO> actividadesLista = estructuraDAO.statisticsDependence(entity.getId());
        return buildStactic(actividadesLista);
    }

    /**
     * Método para construir estadísticas basadas en lista de actividades para una dependencia
     *
     * @param actividades, lista de actidades registradas para una dependencia
     * @return estadistica, objeto con la infomacion procesada
     */
    private List<ActividadOutDTO> buildStactic(List<ActividadDTO> actividades) {
        List<ActividadOutDTO> estadistica = new ArrayList<>();
        Map<String, ActividadOutDTO> estadisticaMap = new HashMap<>();

        List<NivelEntity> niveles = nivelService.findAll();

        for (NivelEntity nivel : niveles) {
            ActividadOutDTO actividad = initializeActividadOutDTO(nivel);
            estadistica.add(actividad);
            estadisticaMap.put(nivel.getDescripcion(), actividad);
        }

        for (ActividadDTO act : actividades) {
            double tiempoEstandar = 0.0;
            double tiempoTarea = 0.0;
            if (act.getTiempoMaximo() != null && act.getTiempoMaximo() > 0 && act.getTiempoMinimo() != null && act.getTiempoMinimo() > 0 && act.getFrecuencia() != null && act.getFrecuencia() > 0) {
                tiempoEstandar = (1.07 * ((act.getTiempoMinimo() + (4.0 * act.getTiempoPromedio()) + act.getTiempoMaximo()) / 6.0));
                tiempoTarea = Math.round((act.getFrecuencia() * tiempoEstandar) * 100.0) / 100.0;
                ActividadOutDTO actividadOutDTO = estadisticaMap.get(act.getNivel());
                actividadOutDTO.setFrecuencia(actividadOutDTO.getFrecuencia() + act.getFrecuencia());
                actividadOutDTO.setTiempoMaximo(actividadOutDTO.getTiempoMaximo() + act.getTiempoMaximo());
                actividadOutDTO.setTiempoMinimo(actividadOutDTO.getTiempoMinimo() + act.getTiempoMinimo());
                actividadOutDTO.setTiempoUsual(actividadOutDTO.getTiempoUsual() + act.getTiempoPromedio());
                actividadOutDTO.setTiempoEstandar(actividadOutDTO.getTiempoEstandar() + tiempoEstandar);
                actividadOutDTO.setTiempoTotalTarea(actividadOutDTO.getTiempoTotalTarea() + tiempoTarea);
            }
        }
        return estadistica;
    }

    /**
     * Método que permite inicializar un objeto tipo ActividadOutDTO para poder realizar los calculos
     *
     * @param nivel, objetos con los informacion de los niveles profesionales
     * @return actividad, lista con los valores iniciales del objeto
     */
    private static ActividadOutDTO initializeActividadOutDTO(NivelEntity nivel) {
        ActividadOutDTO actividad = new ActividadOutDTO();
        actividad.setNivel(nivel.getDescripcion());
        actividad.setExtrae(nivel.getDescripcion().substring(0, Math.min(nivel.getDescripcion().length(), 3)).toUpperCase());
        actividad.setFrecuencia(0.0);
        actividad.setTiempoMaximo(0.0);
        actividad.setTiempoMinimo(0.0);
        actividad.setTiempoUsual(0.0);
        actividad.setTiempoEstandar(0.0);
        actividad.setTiempoTotalTarea(0.0);
        return actividad;
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
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public List<DependenciaDTO> findAllDependencies() {
        return estructuraDAO.findAllDependencies();
    }

    @Override
    public List<EstructuraEntity> findByIdPadre(Long idPadre) {
        return estructuraDAO.findByIdPadre(idPadre);
    }

    @Override
    public Long findTypologyById(Long id) {
        return estructuraDAO.findTypologyById(id);
    }
}
