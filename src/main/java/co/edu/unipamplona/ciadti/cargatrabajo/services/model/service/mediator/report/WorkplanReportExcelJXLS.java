package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.jxls.functions.DoubleSummarizerBuilder;
import org.jxls.functions.GroupSum;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EtapaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PlanTrabajoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.SeguimientoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TareaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EtapaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PlanTrabajoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.SeguimientoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TareaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.StaticResourceMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.EachMergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.HeaderCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.ImageCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.MergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.StyleCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.summarizer.IntegerSummarizerBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WorkplanReportExcelJXLS {
    private final PlanTrabajoService planTrabajoService;
    private final ConfigurationMediator configurationMediator;
    private final StaticResourceMediator staticResourceMediator;
    private Map<String, Object> registry;

    public byte[] generate(List<Long> stageIds, Long idWorkplan) throws Exception{
        registry = new HashMap<>();

        generateDataset(stageIds, idWorkplan);

        String filePath = "reports/workplans/Workplans.xlsx";
        Map<String, Object> contextMap = new HashMap<String, Object>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String reportDate = dateFormat.format(new Date());

        GroupSum<Integer> g = new GroupSum<Integer>(new IntegerSummarizerBuilder());
        contextMap.put("G", g);
        contextMap.put("logo", this.staticResourceMediator.getResourceBytes("reports/images/logo.png"));
        contextMap.put("reportDate", reportDate);
        contextMap.put("workplan", registry.get("workplan"));
        contextMap.put("dates", registry.get("dates"));
        contextMap.put("stages", registry.get("stages"));
        contextMap.put("totalDays", registry.get("totalDays"));
        contextMap.put("workplanAdvance", registry.get("workplanAdvance"));
        Resource resource = this.staticResourceMediator.getResource(filePath);

        byte[] bytes = JxlsPoiTemplateFillerBuilder
            .newInstance()
            .withTemplate(resource.getInputStream())
            .withCommand("merge", MergeCommand.class)
            .withCommand("image", ImageCommand.class)
            .withCommand("style", StyleCommand.class)
            .withCommand("eachMerge", EachMergeCommand.class)
            .withCommand("header", HeaderCommand.class)
            .needsPublicContext(g)
            .buildAndFill(contextMap);
        return bytes;
    }

    private void generateDataset(List<Long> stageIds, Long idWorkplan) throws CiadtiException{
        List<EtapaEntity> stages = null;
        PlanTrabajoEntity workplan = null;
        if (stageIds != null && stageIds.size() > 0 ){
            stages = configurationMediator.findAllStagesByIds(stageIds);
            workplan = planTrabajoService.findByIdStage(stageIds.get(0));
        }else{
            stages = configurationMediator.findAllStagesByIdWorkplan(idWorkplan);
            workplan = planTrabajoService.findById(idWorkplan);
        }
        orderByStartDate(stages);

        Double workplanAdvance = 0.0;
        List<EtapaEntity> plainedStages = new ArrayList<>();
        if (stages != null && stages.size() > 0){
            workplanAdvance = stages.stream()
                            .map(EtapaEntity::getAvance)
                            .filter(avance -> avance != null)
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
            plainByStage(stages, plainedStages, 0);
        }
        registry.put("plainedStages", plainedStages);
        registry.put("workplan", workplan);
        registry.put("workplanAdvance", workplanAdvance);
        registry.put("dates", buildReportHead());
        registry.put("stages", buildReportTaskBody());
    }

    private List<?> buildReportHead(){
        List<YearDTO> years = new ArrayList<>();
        List<EtapaEntity> stages = (List<EtapaEntity>) registry.get("plainedStages");

        Date start = catchStartDate(stages);
        Date end = catchEndDate(stages);
        LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<YearMonth> monthsBetween = getMonthsBetween(startDate, endDate);
        Map<Integer, Map<Month, List<Integer>>> daysByYearAndMonth = getDaysByYearAndMonth(monthsBetween);
       /*En la posición o almacena los días de cada año de acuerdo con los meses establecidos por las fechas, y en la pocisión 1 se almacenan los días totales de todos los años*/
        int[] dayNumbers = new int[2]; 
        
        daysByYearAndMonth.forEach((year, months) -> {
            List<YearDTO.Month> meses =  new ArrayList<>();
            months.forEach((month, days) -> {
                String monthName = month.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                meses.add(YearDTO.Month.builder().value(monthName.toUpperCase()).days(days).build());
                dayNumbers[0] += days.size();
                dayNumbers[1] += days.size();
            });
            years.add(YearDTO.builder().value(year).numberOfDays(dayNumbers[0]).months(meses).build());
        });
       
        registry.put("daysByYearAndMonth", daysByYearAndMonth);
        registry.put("totalDays", dayNumbers[1]);
        return years;
    }

    private List<EtapaEntity> buildReportTaskBody(){
        Map<Integer, Map<Month, List<Integer>>> daysByYearAndMonth  = (Map<Integer, Map<Month, List<Integer>>>) registry.get("daysByYearAndMonth");

        List<EtapaEntity> stages = (List<EtapaEntity>)registry.get("plainedStages");
        for (EtapaEntity stage: stages) {
            
            if (stage.getTareas() != null && stage.getTareas().size()>0){
                stage.getTareas().forEach(e -> {
                    LocalDate start = e.getFechaInicio().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate end = e.getFechaFin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    int startYear = start.getYear();
                    int startMonth = start.getMonthValue();
                    int startDay = start.getDayOfMonth();
                    int endYear = end.getYear();
                    int endMonth = end.getMonthValue();
                    int endDay = end.getDayOfMonth();
        
                    e.setDiasReporte(new ArrayList<>());
                    boolean included = false;
                    for (Map.Entry<Integer, Map<Month, List<Integer>>> yearEntry : daysByYearAndMonth.entrySet()) {
                        int year = yearEntry.getKey();
                        Map<Month, List<Integer>> monthsMap = yearEntry.getValue();
                        for (Map.Entry<Month, List<Integer>> monthEntry : monthsMap.entrySet()) {
                            Month month = monthEntry.getKey();
                            List<Integer> daysList = monthEntry.getValue();
                            for (Integer day : daysList) {
                                if (year == startYear && month.getValue() == startMonth && day == startDay){
                                    included = true;
                                }
                                e.getDiasReporte().add(included);
                                if (year == endYear && month.getValue() == endMonth && day == endDay){
                                    included = false;
                                }
                            }
                        }
                    }
                    SeguimientoEntity lastFollowUp = getLastFollowUp(e.getSeguimientos());
                    String observation = lastFollowUp != null ? lastFollowUp.getObservacion() : null;   
                    e.setObservacionGeneral(observation);
                });        
            }
        }       
        
        return stages;
    }

    private void plainByStage(List<EtapaEntity> stages, List<EtapaEntity> plainedStages, Integer levelSubstage){
        if (stages != null){
            for (EtapaEntity e : stages) {
                e.setLevel(levelSubstage);
                plainedStages.add(e);
                plainByStage(e.getSubEtapas(), plainedStages, levelSubstage + 1);
            }
        }
    }

    /**
     * Encuentra la fecha de inicio más temprana entre todas las tareas de las etapas proporcionadas.
     * @param stages La lista de etapas de las cuales se buscará la fecha de inicio más temprana.
     * @return La fecha de inicio más temprana encontrada entre todas las tareas, o la fecha máxima si no hay tareas.
     */
    private Date catchStartDate(List<EtapaEntity> stages){
        Date startDate = new Date(Long.MAX_VALUE);
        for (EtapaEntity stage : stages) {
            if (stage.getTareas() != null && stage.getTareas().size() > 0){
                Date tempDate = Collections.min(stage.getTareas().stream().map(e -> e.getFechaInicio()).toList());
                if (tempDate.before(startDate)){
                    startDate = tempDate;
                }
            }
        }
        return startDate;
    }

    /**
     * Encuentra la fecha de fin más tardía entre todas las tareas de las etapas proporcionadas.
     * @param stages La lista de etapas de las cuales se buscará la fecha de fin más tardía.
     * @return La fecha de fin más tardía encontrada entre todas las tareas, o la fecha mínima si no hay tareas.
     */
    private Date catchEndDate(List<EtapaEntity> stages){
        Date endDate = new Date(Long.MIN_VALUE);
        for (EtapaEntity stage : stages) {
            if (stage.getTareas() != null && stage.getTareas().size() > 0){
                Date tempDate = Collections.max(stage.getTareas().stream().map(e -> e.getFechaFin()).toList());
                if (tempDate.after(endDate)){
                    endDate = tempDate;
                }
            }
        }
        return endDate;
    }

    /**
     * Calcula todos los meses entre dos fechas (incluyendo los meses de las fechas de inicio y fin).
     * @param startDate La fecha de inicio.
     * @param endDate La fecha de fin.
     * @return Una lista de objetos YearMonth representando cada mes entre startDate y endDate.
     */
    private List<YearMonth> getMonthsBetween(LocalDate startDate, LocalDate endDate) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth start = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);
        while (!start.isAfter(end)) {
            months.add(start);
            start = start.plusMonths(1);
        }
        return months;
    }

    /**
     * Agrupa los días de cada mes por año en un mapa anidado.
     * @param months Una lista de objetos YearMonth representando los meses a procesar.
     * @return Un mapa que mapea los años a un segundo mapa que mapea los meses a una lista de días.
     */
    private Map<Integer, Map<Month, List<Integer>>> getDaysByYearAndMonth(List<YearMonth> months) {
        Map<Integer, Map<Month, List<Integer>>> daysByYearAndMonth = new LinkedHashMap<>();
        for (YearMonth yearMonth : months) {
            int year = yearMonth.getYear();
            Month month = yearMonth.getMonth();
            List<Integer> days = getDaysOfMonth(yearMonth);

            daysByYearAndMonth
                .computeIfAbsent(year, k -> new LinkedHashMap<>())
                .put(month, days);
        }
        return daysByYearAndMonth;
    }

    /**
     * Obtiene todos los días del mes dado.
     * @param yearMonth El objeto YearMonth que representa el mes.
     * @return Una lista de enteros que representan los días del mes dado.
     */
    private List<Integer> getDaysOfMonth(YearMonth yearMonth) {
        List<Integer> days = new ArrayList<>();
        int lengthOfMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= lengthOfMonth; day++) {
            days.add(day);
        }
        return days;
    }


    /**
     * Ordena una lista de EtapaEntity por la fecha de inicio más antigua de sus tareas o subetapas.
     * @param items Lista de EtapaEntity que se va a ordenar.
     */
    public void orderByStartDate(List<EtapaEntity> items) {
        for (EtapaEntity item : items) {
            sortTasks(item);
        }
        items.sort(Comparator.comparing(this::findStartDate, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /**
     * Ordena las tareas dentro de una EtapaEntity por fecha de inicio.
     * Además, ordena recursivamente las tareas dentro de las subetapas.
     * @param item La EtapaEntity cuyas tareas y subetapas serán ordenadas.
     */
    private void sortTasks(EtapaEntity item) {
        if (item.getTareas() != null) {
            item.getTareas().sort(Comparator.comparing(TareaEntity::getFechaInicio, Comparator.nullsLast(Comparator.naturalOrder())));
        }
        if (item.getSubEtapas() != null) {
            for (EtapaEntity subItem : item.getSubEtapas()) {
                sortTasks(subItem);
            }
            item.getSubEtapas().sort(Comparator.comparing(this::findStartDate, Comparator.nullsLast(Comparator.naturalOrder())));
        }
    }

    /**
     * Encuentra la fecha de inicio más temprana dentro de una EtapaEntity o sus subetapas recursivamente.
     * @param item La EtapaEntity de la cual se buscará la fecha de inicio más antigua.
     * @return La fecha de inicio más temprana encontrada, o null si no hay fechas disponibles.
     */
    private Date findStartDate(EtapaEntity item) {
        Date startDate = null;
        if (item.getTareas() != null && !item.getTareas().isEmpty()) {
            startDate = item.getTareas().get(0).getFechaInicio();
        }
        if (item.getSubEtapas() != null) {
            for (EtapaEntity subItem : item.getSubEtapas()) {
                Date subStartDate = findStartDate(subItem);
                if (subStartDate != null) {
                    if (startDate == null || subStartDate.before(startDate)) {
                        startDate = subStartDate;
                    }
                }
            }
        }
        return startDate;
    }

    /**
     * Obtiene el último seguimiento en orden cronológico de una lista de seguimientos realizados a una tarea
     * @param follows: Lista de seguimientos
     * @return SeguimientoEntity: Objeto con información del seguimiento
     */
    private SeguimientoEntity getLastFollowUp(List<SeguimientoEntity> follows) {
        if (follows == null || follows.isEmpty()){
            return null;
        }
        Optional<SeguimientoEntity> last = follows.stream().max(Comparator.comparing(SeguimientoEntity::getFecha));
        return last.orElse(null);
    }
}
