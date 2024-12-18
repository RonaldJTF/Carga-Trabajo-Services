package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.TimeStatisticDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.ActividadDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ActividadService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Corporate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class StatisticsMediator {

    private final NivelService nivelService;
    private final EstructuraService estructuraService;
    private final ActividadService actividadService;

    /**
     * Obtiene la información estadística de los tiempos laborados para una estructura por niveles ocupacionales.
     * @param entity, objeto con el identificador único de la estructura.
     * @return lista de objetos con información estadística para la estructura consultada.
     */
    @Transactional(readOnly = true)
    public List<TimeStatisticDTO> getTimeStatistics(EstructuraEntity entity) {
        List<ActividadDTO> actividadesLista = estructuraService.getTimeStatistics(entity.getId());
        return buildTimeStatistic(actividadesLista);
    }
    
    /**
     * Calcula las estadísticas de tiempos laborados basadas en lista de actividades en una estructura.
     * @param activities, lista de actividades registradas para una estructura.
     * @return: objeto con la información estadística asociada por nivel.
     */
    private List<TimeStatisticDTO> buildTimeStatistic(List<ActividadDTO> activities) {
        List<TimeStatisticDTO> timeStatistics = new ArrayList<>();
        Map<String, TimeStatisticDTO> statisticsByLevel = new HashMap<>();

        List<NivelEntity> levels = nivelService.findAll();

        Double tiempoTotalGlobal = actividadService.getGlobalTotalTime() / 60.0;

        for (NivelEntity level : levels) {
            TimeStatisticDTO e = TimeStatisticDTO
                .builder()
                .nivel(level.getDescripcion())
                .frecuencia(0.0)
                .tiempoMinimo(0.0)
                .tiempoMaximo(0.0)
                .tiempoUsual(0.0)
                .tiempoTotal(0.0)
                .personalTotal(0.0)
                .tiempoTotalGlobal(tiempoTotalGlobal)
                .personalTotalGlobal(tiempoTotalGlobal / Corporate.MONTHLY_WORKING_TIME.getValue())
                .build();
            timeStatistics.add(e);
            statisticsByLevel.put(level.getDescripcion(), e);
        }

        double tiempoEstandar;
        double tiempoTotal;
        double personalTotal;
        for (ActividadDTO act : activities) {
            if (act.getTiempoMaximo() != null && act.getTiempoMinimo() != null && act.getFrecuencia() != null) {
                tiempoEstandar = 1.07 * (act.getTiempoMinimo() + 4.0 * act.getTiempoPromedio() + act.getTiempoMaximo()) / 6.0;
                tiempoTotal = act.getFrecuencia() * tiempoEstandar / 60.0;
                personalTotal = tiempoTotal / Corporate.MONTHLY_WORKING_TIME.getValue();
                
                TimeStatisticDTO dto = statisticsByLevel.get(act.getNivel());
                dto.setFrecuencia(dto.getFrecuencia() + act.getFrecuencia());
                dto.setTiempoMaximo(dto.getTiempoMaximo() + act.getTiempoMaximo());
                dto.setTiempoMinimo(dto.getTiempoMinimo() + act.getTiempoMinimo());
                dto.setTiempoUsual(dto.getTiempoUsual() + act.getTiempoPromedio());
                dto.setTiempoTotal(dto.getTiempoTotal() + tiempoTotal);
                dto.setPersonalTotal(dto.getPersonalTotal() + personalTotal);
            }
        }
        return timeStatistics;
    }

}
