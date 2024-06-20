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
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.BlockPOI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.CellPOI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.Position;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.ReportPOI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.Style;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WorkplanReportExcel {
    private final PlanTrabajoService planTrabajoService;
    private final EtapaService etapaService;
    private final TareaService tareaService;
    private final SeguimientoService seguimientoService;
    private final ConfigurationMediator configurationMediator;
    private final ResourceLoader resourceLoader;

    private Map<String, Object> registry;

    private final int[] GRAY = {242, 242, 242};
    private final int[] GREEN = {238, 245, 208};
    private final int[] WHITE = {255, 255, 255};
    private final int[] BLUE = {208, 224, 245};
    private final int[] AQUA = {233, 255, 253};
    private final int[] PINK = {245, 212, 208};

    private final int[][] SUBSTAGE_COLORS = {
        {245, 182, 172}, // Rosa más oscuro
        {245, 212, 208}, // Rosa medio
        {245, 242, 238}  // Rosa más claro
    };
    
    private final Boolean[] BORDER_TOP = {true, false, false, false};
    private final Boolean[] BORDER_RIGHT = {false, true, false, false};
    private final Boolean[] BORDER_BOTTOM = {false, false, true, false};
    private final Boolean[] BORDER_LEFT = {false, false, false, true};

    public byte[] generate(List<Long> structureIds, Long idWorkplan) throws CiadtiException{
        registry = new HashMap<>();

        generateDataset(structureIds, idWorkplan);

        BlockPOI titleBlock = buildReportTitle(Position.builder().x(1).y(1).build());
        Map<String, Position> positions;
        ReportPOI report = new ReportPOI("Reporte");
        positions = report.addTitle(titleBlock);
        Position bottomTitlePosition = positions.get("bottom");
        bottomTitlePosition.setY(bottomTitlePosition.getY() + 3);
        positions = report.addBlock(buildReportHead(bottomTitlePosition));
        Position TopRightHeadPosition = positions.get("top-right");
        TopRightHeadPosition.setY(TopRightHeadPosition.getY() - 1);
        TopRightHeadPosition.setX(TopRightHeadPosition.getX() - 1);
        report.addBlock(buildReportDate(TopRightHeadPosition));
        

        List<EtapaEntity> stages = (List<EtapaEntity>)registry.get("plainedStages");
        for (EtapaEntity e : stages) {
            positions = report.addBlock(buildReportStageBody(positions.get("bottom"), e));
            if (e.getTareas() != null && e.getTareas().size()>0){
                positions = report.addBlock(buildReportTaskBody(positions.get("bottom"), e));
            }
        }

        positions = report.addBlock(buildReportResume(positions.get("bottom")));
        report.builtSheetContent();
        return report.generateBytes();
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

        Double advance = 0.0;
        List<EtapaEntity> plainedStages = new ArrayList<>();
        if (stages != null && stages.size() > 0){
            advance = stages.stream()
                            .map(EtapaEntity::getAvance)
                            .filter(avance -> avance != null)
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
            plainByStage(stages, plainedStages, 0);
        }
        registry.put("plainedStages", plainedStages);
        registry.put("workplan", workplan);
        registry.put("advance", advance);
    }

    private BlockPOI buildReportTitle(Position position) throws CiadtiException{
        PlanTrabajoEntity workplan = (PlanTrabajoEntity) registry.get("workplan");
        String description = workplan.getDescripcion() != null && !workplan.getDescripcion().isEmpty() ? workplan.getDescripcion() : "Plan de trabajo";
        return BlockPOI.builder()
            .position(position)
            .style(Style.builder().backgroundColorRGB(GRAY).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER).build())
            .items(List.of(
                    CellPOI.builder().value(getImageBytes()).style(Style.builder().width(20).borderStyle(BorderStyle.MEDIUM).build()).build(),
                    CellPOI.builder().value(workplan.getNombre()).style(Style.builder().font("Arial Black").fontSize(14).borderStyle(BorderStyle.MEDIUM).borders(new Boolean[]{true, true, false, false}).verticalAlignment(VerticalAlignment.BOTTOM).height((short)30).build())
                        .children(CellPOI.createSiblings(List.of(
                            CellPOI.builder().value(description).style(Style.builder().fontSize(10).borders(new Boolean[]{false, true, true, false}).verticalAlignment(VerticalAlignment.TOP).height((short)30).build()).build()))).build())
            ).build();
    }

    private byte[] getImageBytes(){
        Resource resource = resourceLoader.getResource("classpath:reports/images/logo.png");
        InputStream inputStream;
        try {
            inputStream = resource.getInputStream();
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BlockPOI buildReportDate(Position position){    
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String date = dateFormat.format(new Date());
        return BlockPOI.builder()
            .position(position)
            .style(Style.builder().backgroundColorRGB(GRAY).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER).borderStyle(BorderStyle.THIN).wrapText(true).build())
            .items(List.of(
                CellPOI.builder().value("Fecha del reporte").build(),
                CellPOI.builder().value(date).build()
            )).build();
    }

    private BlockPOI buildReportHead(Position position){
        List<CellPOI> items = new ArrayList<>();
        List<EtapaEntity> stages = (List<EtapaEntity>) registry.get("plainedStages");

        Date start = catchStartDate(stages);
        Date end = catchEndDate(stages);

        LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<YearMonth> monthsBetween = getMonthsBetween(startDate, endDate);
        Map<Integer, Map<Month, List<Integer>>> daysByYearAndMonth = getDaysByYearAndMonth(monthsBetween);
        int[] daysNumber = new int[1];
        List<CellPOI> yearHeads = new ArrayList<>();

        daysByYearAndMonth.forEach((year, months) -> {
            List<CellPOI> monthHeads =  new ArrayList<>();
            yearHeads.add(CellPOI.builder().value(year).children(monthHeads).build());
            months.forEach((month, days) -> {
                List<CellPOI> dayHeads = new ArrayList<>();
                String monthName = month.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                monthHeads.add(CellPOI.builder().value(monthName.toUpperCase()).children(dayHeads).build());
                days.forEach(day -> {
                    dayHeads.add(CellPOI.builder().value(day).style(Style.builder().width(2).rotation((short)90).bold(false).fontSize(6).build()).build());
                    daysNumber[0] += 1;
                });
            });
        });
        items.add(CellPOI.builder().value("TAREA").build());
        items.add(CellPOI.builder().value("RESPONSABLE").build());
        items.add(CellPOI.builder().value("ENTREGABLE").build());
        items.add(CellPOI.builder().value("FECHA DE REALIZACIÓN").style(Style.builder().backgroundColorRGB(GREEN).build()).children(yearHeads).build());
        items.add(CellPOI.builder().value("OBSERVACIÓN").build());
        items.add(CellPOI.builder().value("AVANCE").style(Style.builder().width(20).build()).build());

        registry.put("daysByYearAndMonth", daysByYearAndMonth);
        registry.put("daysNumber", daysNumber[0]);

        BlockPOI block = BlockPOI.builder()
            .position(position)
            .items(items)
            .style(
                Style.builder().backgroundColorRGB(BLUE).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER)
                    .bold(true).borderStyle(BorderStyle.THIN).wrapText(true).width(40).build())
            .build();
        return block;
    }

    private BlockPOI buildReportStageBody(Position position, EtapaEntity stage){
        List<CellPOI> items = new ArrayList<>();
        Integer daysNumber = (Integer) registry.get("daysNumber");
        Double advance = stage.getAvance() != null ? stage.getAvance() : 0.0;
        int[] advanceColor = Methods.getColorFromPercentage(advance);
        int[] color = GRAY;
        if (SUBSTAGE_COLORS.length > stage.getLevel()){
            color = SUBSTAGE_COLORS[stage.getLevel()];
        }
        items.add(CellPOI.builder().value(stage.getNombre()).aditionalCellsToUse(2).build());        
        items.add(CellPOI.builder().aditionalCellsToUse(daysNumber - 1).style(Style.builder().patternType(FillPatternType.THICK_BACKWARD_DIAG).build()).build());
        items.add(CellPOI.builder().style(Style.builder().patternType(FillPatternType.THICK_FORWARD_DIAG).build()).build());  
        items.add(CellPOI.builder().value(advance).style(Style.builder().backgroundColorRGB(advanceColor).build()).build());  
        return BlockPOI.builder()
            .position(position)
            .items(items)
            .showInColumn(false)
            .style(Style.builder().backgroundColorRGB(color).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER).borderStyle(BorderStyle.THIN).build())
            .build();
    }

    private BlockPOI buildReportTaskBody(Position position, EtapaEntity stage){
        List<CellPOI> items = new ArrayList<>();
        Map<Integer, Map<Month, List<Integer>>> daysByYearAndMonth  = (Map<Integer, Map<Month, List<Integer>>>) registry.get("daysByYearAndMonth");
        
        stage.getTareas().forEach(e -> {
            List<CellPOI> siblings = new ArrayList<>(List.of(
                CellPOI.builder().value(e.getResponsable()).build(),
                CellPOI.builder().value(e.getEntregable()).build()
            ));
            int[] advanceColor = Methods.getColorFromPercentage(e.getAvance());
            LocalDate start = e.getFechaInicio().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = e.getFechaFin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int startYear = start.getYear();
            int startMonth = start.getMonthValue();
            int startDay = start.getDayOfMonth();
            int endYear = end.getYear();
            int endMonth = end.getMonthValue();
            int endDay = end.getDayOfMonth();
            for (Map.Entry<Integer, Map<Month, List<Integer>>> yearEntry : daysByYearAndMonth.entrySet()) {
                int year = yearEntry.getKey();
                Map<Month, List<Integer>> monthsMap = yearEntry.getValue();
                for (Map.Entry<Month, List<Integer>> monthEntry : monthsMap.entrySet()) {
                    Month month = monthEntry.getKey();
                    List<Integer> daysList = monthEntry.getValue();
                    boolean draw = false;
                    for (Integer day : daysList) {
                        if (year == startYear && month.getValue() == startMonth && day == startDay){
                            draw = true;
                        }
                        siblings.add(
                            CellPOI.builder().style(
                                Style.builder()
                                    .borderStyle(BorderStyle.THIN)
                                    .borders(new Boolean[]{true, false, true, false})//Top Right Bottom Left
                                    .backgroundColorRGB(draw ? BLUE : WHITE)
                                    .patternType(draw ? FillPatternType.SOLID_FOREGROUND : FillPatternType.NO_FILL).build()).build());
                        if (year == endYear && month.getValue() == endMonth && day == endDay){
                            draw = false;
                        }
                    }
                }
            }
            SeguimientoEntity lastFollowUp = getLastFollowUp(e.getSeguimientos());
            String observation = lastFollowUp != null ? lastFollowUp.getObservacion() : null;   
            siblings.add(CellPOI.builder().value(observation).style(Style.builder().patternType(FillPatternType.NO_FILL).borderStyle(BorderStyle.THIN).borders(new Boolean[]{true, true, true, true}).build()).build());  
            siblings.add(CellPOI.builder().value(e.getAvance()).style(Style.builder().patternType(FillPatternType.SOLID_FOREGROUND).backgroundColorRGB(advanceColor).borderStyle(BorderStyle.THIN).horizontalAlignment(HorizontalAlignment.CENTER).build()).build());  
            items.add(
                CellPOI.builder()
                .value(e.getNombre())
                .children(CellPOI.createSiblings(siblings))
                .build());
        });
        
        return BlockPOI.builder()
            .position(position)
            .items(items)
            .showInColumn(true)
            .style(Style.builder().backgroundColorRGB(WHITE).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER)
                .borderStyle(BorderStyle.THIN).build())
            .build();
    }

    private BlockPOI buildReportResume(Position position){
        List<CellPOI> items = new ArrayList<>();
        Integer daysNumber = (Integer) registry.get("daysNumber");
        Double workplanAdvance = (Double) registry.get("advance");
        int[] advanceColor = Methods.getColorFromPercentage(workplanAdvance);
        
        items.add(CellPOI.builder().value("PORCENTAJE DE AVANCE GLOBAL").aditionalCellsToUse(2).style(Style.builder().horizontalAlignment(HorizontalAlignment.LEFT).build()).build());        
        items.add(CellPOI.builder().aditionalCellsToUse(daysNumber - 1).style(Style.builder().patternType(FillPatternType.THICK_BACKWARD_DIAG).build()).build());
        items.add(CellPOI.builder().style(Style.builder().patternType(FillPatternType.THICK_FORWARD_DIAG).build()).build());  
        items.add(CellPOI.builder().value(workplanAdvance).style(Style.builder().backgroundColorRGB(advanceColor).build()).build());  
        return BlockPOI.builder()
            .position(position)
            .items(items)
            .showInColumn(false)
            .style(Style.builder().backgroundColorRGB(BLUE).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER).borderStyle(BorderStyle.THIN).bold(true).build())
            .build();
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
