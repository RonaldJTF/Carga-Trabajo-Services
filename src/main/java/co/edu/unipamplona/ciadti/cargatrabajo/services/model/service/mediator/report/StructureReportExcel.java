package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Trace;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Corporate;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.BlockPOI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.CellPOI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.Position;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.ReportPOI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.Style;
import lombok.RequiredArgsConstructor;

@Deprecated
@RequiredArgsConstructor
@Service
public class StructureReportExcel {
    private final EstructuraService estructuraService;
    private final TipologiaService tipologiaService;
    private final NivelService nivelService;
    private final ResourceLoader resourceLoader;

    private Map<String, Object> registry;
    private Double HOURS_PER_MONTH;

    private final int[] GRAY = {242, 242, 242};
    private final int[] GREEN = {238, 245, 208};
    private final int[] WHITE = {255, 255, 255};
    private final int[] BLUE = {208, 224, 245};
    private final int[] AQUA = {233, 255, 253};
    private final int[] PINK = {245, 212, 208};

    public byte[] generate(List<Long> structureIds) throws CiadtiException{
        registry = new HashMap<>();
        HOURS_PER_MONTH = Corporate.MONTHLY_WORKING_TIME.getValue();
        generateDataset(structureIds);

        BlockPOI titleBlock = buildDependencyReportTitle(Position.builder().x(1).y(1).build());
        Map<String, Position> positions;
        ReportPOI report = new ReportPOI("Detalle");
        positions = report.addTitle(titleBlock);
        Position bottomTitlePosition = positions.get("bottom");
        bottomTitlePosition.setY(bottomTitlePosition.getY() + 3);
        positions = report.addBlock(buildDependencyReportHead(bottomTitlePosition));
        Position TopRightHeadPosition = positions.get("top-right");
        TopRightHeadPosition.setY(TopRightHeadPosition.getY() - 1);
        TopRightHeadPosition.setX(TopRightHeadPosition.getX() - 3);
        report.addBlock(buildDependencyReportDate(TopRightHeadPosition, 1));
        positions = report.addBlock(buildDependencyReportStructure(structureIds, positions.get("bottom")));
        positions = report.addBlock(buildDependencyReportResume(positions.get("bottom")));
        report.builtSheetContent();

        report.createSheet("Consolidado");
        titleBlock = buildDependencyReportTitle(Position.builder().x(1).y(1).build());
        positions = report.addTitle(titleBlock);
        Position bottomConsolidatedTitlePosition = positions.get("bottom");
        bottomConsolidatedTitlePosition.setY(bottomConsolidatedTitlePosition.getY() + 3);
        positions = report.addBlock(buildConsolidatedReportHead(bottomConsolidatedTitlePosition));
        TopRightHeadPosition = positions.get("top-right");
        TopRightHeadPosition.setY(TopRightHeadPosition.getY() - 1);
        TopRightHeadPosition.setX(TopRightHeadPosition.getX() - 1);
        report.addBlock(buildDependencyReportDate(TopRightHeadPosition, 0));
        positions = report.addBlock(buildConsolidatedReportBody(structureIds, positions.get("bottom")));
        positions = report.addBlock(buildConsolidatedReportResume(positions.get("bottom")));
        report.builtSheetContent();
        return report.generateBytes();
    }

    private void generateDataset(List<Long> structureIds) throws CiadtiException{
        List<TipologiaEntity> typologies = tipologiaService.findAllManagement(); 
        List<NivelEntity> levels = nivelService.findAll();
        TipologiaEntity tipologiaEntity = tipologiaService.findFirstTipology();
        List<EstructuraEntity> structures = structureIds != null && structureIds.size() > 0 
                                            ? estructuraService.findAllFilteredByIds(structureIds) 
                                            : estructuraService.findAllFilteredBy(EstructuraEntity.builder().nombre("").build());
        List<EstructuraEntity> plainedStructures = new ArrayList<>();
        filterAndPlainByTypologyId(structures, plainedStructures, tipologiaEntity.getId());
        filterDistinctOfTypologyId(plainedStructures, tipologiaEntity.getId());

        registry.put("plainedStructures", plainedStructures);
        registry.put("levels", levels);
        registry.put("typologies", typologies);
    }

    private BlockPOI buildDependencyReportTitle(Position position) throws CiadtiException{
        return BlockPOI.builder()
            .position(position)
            .style(Style.builder().backgroundColorRGB(GRAY).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER).build())
            .items(List.of(
                    CellPOI.builder().value(getImageBytes()).style(Style.builder().width(20).borderStyle(BorderStyle.MEDIUM).build()).build(),
                    CellPOI.builder().value("Gestión y Desarrollo del Talento Humano").style(Style.builder().font("Arial Black").fontSize(14).borderStyle(BorderStyle.MEDIUM).borders(new Boolean[]{true, true, false, false}).verticalAlignment(VerticalAlignment.BOTTOM).height((short)30).build())
                        .children(CellPOI.createSiblings(List.of(
                            CellPOI.builder().value("Gestión de Tiempos Laborados por Dependencia").style(Style.builder().fontSize(12).borders(new Boolean[]{false, true, true, false}).verticalAlignment(VerticalAlignment.TOP).height((short)30).build()).build()
                        ))).build())
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
            Trace.logError(this.getClass().getName(), Methods.getCurrentMethodName(this.getClass()), e);
        }
        return null;
    }

    private BlockPOI buildDependencyReportDate(Position position, int aditionalCeld){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String date = dateFormat.format(new Date());
        return BlockPOI.builder()
            .position(position)
            .style(Style.builder().backgroundColorRGB(GRAY).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER).borderStyle(BorderStyle.THIN).wrapText(true).build())
            .items(List.of(
                CellPOI.builder().value("Fecha del reporte").aditionalCellsToUse(aditionalCeld).build(),
                CellPOI.builder().value(date).aditionalCellsToUse(aditionalCeld).build()
            )).build();
    }

    private BlockPOI buildDependencyReportHead(Position position){
        List<CellPOI> items = new ArrayList<>();
        List<TipologiaEntity> typologies = (List<TipologiaEntity>)registry.get("typologies");
        List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
        List<EstructuraEntity> plainedStructures = (List<EstructuraEntity>)registry.get("plainedStructures");

        int totalDeep = 0;
        List<CellPOI> subItemHeads = null;
        for (TipologiaEntity e : typologies){
            int deep = getMaxDeepOfTipology(plainedStructures, e.getId(), 0, 0);
            if (deep > 1){
                totalDeep += deep - 1;
                subItemHeads = new ArrayList<>();
                for (int i = 0; i < deep; i++){
                    subItemHeads.add(CellPOI.builder().value("").build());
                }
            }
            items.add(CellPOI.builder().value(e.getNombre().toUpperCase()).children(subItemHeads).build());
        }

        List<CellPOI> levelHeads = new ArrayList<>();
        for (NivelEntity e : levels){
            levelHeads.add(CellPOI.builder().value(getLevelNomenclature(e.getDescripcion())).build());
        }

        registry.put("totalColumns", typologies.size() + levels.size() + 7 + totalDeep);
        registry.put("startColumn", typologies.size() + 7 + totalDeep);
        registry.put("tipologyTotalColumn", typologies.size() + totalDeep);

        items.add(CellPOI.builder().value("NIVEL").style(Style.builder().width(15).backgroundColorRGB(GREEN).build()).build());
        items.add(CellPOI.builder().value("EXTRAE NIVEL").style(Style.builder().width(15).build()).build());
        items.add(CellPOI.builder().value("FRECUENCIA").style(Style.builder().width(15).build()).build());
        items.add(CellPOI.builder().value("Tmin").style(Style.builder().width(10).build()).build());
        items.add(CellPOI.builder().value("Tusual").style(Style.builder().width(10).build()).build());
        items.add(CellPOI.builder().value("Tmáx").style(Style.builder().width(10).build()).build());
        items.add(CellPOI.builder().value("TE (Tiempo Estandar)").style(Style.builder().width(15).build()).build());
        items.add(CellPOI.builder().value("TIEMPO TOTAL POR TAREA").style(Style.builder().backgroundColorRGB(GREEN).build())
                         .children(levelHeads).build());
        BlockPOI block = BlockPOI.builder()
            .position(position)
            .items(items)
            .style(
                Style.builder().backgroundColorRGB(BLUE).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER)
                    .bold(true).borderStyle(BorderStyle.THIN).wrapText(true).width(40).build())
            .build();
        return block;
    }

    /**
     * Calcula la máxima profundidad en una jerarquía de estructuras que contiene una tipología específica.
     * @param structures Una lista de objetos {@code EstructuraEntity} que representan las estructuras a recorrer.
     * @param idTipology El identificador de la tipología que se busca en las estructuras.
     * @param deep La profundidad actual en la jerarquía de estructuras.
     * @param max La profundidad máxima encontrada hasta el momento.
     * @return La máxima profundidad de estructuras que contienen la tipología especificada.
     */
    private int getMaxDeepOfTipology(List<EstructuraEntity> structures, Long idTipology, int deep, int max) {
        for (EstructuraEntity e : structures) {
            int currentDeep = deep;
            if (e.getIdTipologia().equals(idTipology)) {
                currentDeep += 1;
                max = Math.max(max, currentDeep);
            }
            if (e.getSubEstructuras() != null) {
                max = Math.max(getMaxDeepOfTipology(e.getSubEstructuras(), idTipology, currentDeep, max), max);
            }
        }
        return max;
    }
    

    private BlockPOI buildDependencyReportStructure(List<Long> structureIds, Position position){
        List<CellPOI> items = new ArrayList<>();
        List<EstructuraEntity> plainedStructures = (List<EstructuraEntity>)registry.get("plainedStructures");
        int tipologyTotalColumn = (int) registry.get("tipologyTotalColumn");
        addItem(items, plainedStructures, 0, tipologyTotalColumn);
        return BlockPOI.builder()
            .position(position)
            .items(items)
            .showInColumn(true)
            .noExtend(true)
            .style(Style.builder().backgroundColorRGB(WHITE).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER)
                .borderStyle(BorderStyle.THIN).build())
            .build();
    }

    private BlockPOI buildDependencyReportResume(Position position){
        List<CellPOI> items = List.of(
            createTotalCell("TOTAL HORAS REQUERIDAS", "totalTime_", null, 1, PINK, false),
            createTotalCell("PERSONAL TOTAL REQUERIDO", "totalTime_", null, HOURS_PER_MONTH, PINK, false)
        );
        return BlockPOI.builder()
            .showInColumn(true)
            .position(position)
            .items(items)
            .style(Style.builder().patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER).borderStyle(BorderStyle.THIN).bold(true).build())
            .build();
    }

    private void addItem (List<CellPOI> items,  List<EstructuraEntity> structures, int deep, int maxDeep){
        for (EstructuraEntity structure : structures){
            CellPOI cell = CellPOI.builder().style(Style.builder().wrapText(true).build()).value(structure.getNombre()).subValue(structure.getDescripcion()).children(new ArrayList<>()).build();
            items.add(cell);

            if(structure.getSubEstructuras() != null && !structure.getSubEstructuras().isEmpty()){
                addItem(cell.getChildren(), structure.getSubEstructuras(), deep + 1, maxDeep);
            }
                
            if ("1".equals(structure.getTipologia().getEsDependencia())){
                items.add(createTotalCell("Horas requeridas", "temporalTotalTimePerDependency_", null, 1, AQUA, false));
                items.add(createTotalCell("Personal requerido", "temporalTotalTimePerDependency_", null, HOURS_PER_MONTH, AQUA, false));
                List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
                for (NivelEntity e : levels){
                    registry.put("totalTimePerDependency_" + structure.getId() + e.getId(),  registry.get("temporalTotalTimePerDependency_" + e.getId()));
                    registry.put("temporalTotalTimePerDependency_" + e.getId(), null);
                }
            }

            if (structure.getActividad() != null){
                int copy = deep + 1;
                while (maxDeep - copy > 0){
                    List<CellPOI> children = cell.getChildren();
                    cell = CellPOI.builder().value("").children(new ArrayList<>()).build();
                    children.add(cell);
                    copy += 1;
                } 

                List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");

                String levl = structure.getActividad().getNivel().getDescripcion();
                String levelNomenclature = getLevelNomenclature(structure.getActividad().getNivel().getDescripcion());
                Double frecuency = structure.getActividad().getFrecuencia();
                Double minTime = (double) Math.round((structure.getActividad().getTiempoMinimo() / 60.0)*100.0)/100.0;
                Double meanTime = (double) Math.round((structure.getActividad().getTiempoPromedio() / 60.0)*100.0)/100.0;
                Double maxTime = (double) Math.round((structure.getActividad().getTiempoMaximo() / 60.0)*100.0)/100.0;
                Double standarTime = (double) Math.round((1.07*(minTime + 4*meanTime + maxTime)/6)*100.0) /100.0;

                List<CellPOI> timeResume = new ArrayList<>();
                for (NivelEntity e : levels){
                    timeResume.add(CellPOI.builder().value(e.getId() == structure.getActividad().getIdNivel() ? String.valueOf(Math.round((frecuency * standarTime)*100.0)/100.0) : "").build());
                        
                    Double acc = (Double)registry.get("totalTime_"+e.getId());  
                    if (e.getId() == structure.getActividad().getIdNivel()){
                        if (acc == null){
                            acc = 0.0;
                        } 
                        acc += frecuency * standarTime;
                        registry.put("totalTime_"+e.getId(), acc);
                    }
                    
                    Double accPerDependency = (Double)registry.get("temporalTotalTimePerDependency_" + e.getId());  
                    if (e.getId() == structure.getActividad().getIdNivel()){
                        if (accPerDependency == null){
                            accPerDependency = 0.0;
                        } 
                        accPerDependency += frecuency * standarTime;
                        registry.put("temporalTotalTimePerDependency_" + e.getId(), accPerDependency);
                    }
                }

                CellPOI cellStandarTime = CellPOI.builder().value(standarTime).build();
                cellStandarTime.addSiblings(timeResume);
                cell.addSiblings(List.of(
                    CellPOI.builder().value(levl).build(),
                    CellPOI.builder().value(levelNomenclature).build(),
                    CellPOI.builder().value(frecuency).build(),
                    CellPOI.builder().value(minTime).build(),
                    CellPOI.builder().value(meanTime).build(),
                    CellPOI.builder().value(maxTime).build(),
                    cellStandarTime
                ));
            }
        }
    }

    private CellPOI createTotalCell(String label, String accKey, Long idStrucure, double divisor, int[] backgroundColorRGB, boolean hasResume) {
        List<CellPOI> items = new ArrayList<>();
        List<NivelEntity> levels = (List<NivelEntity>) registry.get("levels");
        int startColumn = (int) registry.get("startColumn");
        int totalColumns = (int) registry.get("totalColumns");

        NivelEntity level;
        Map<String, String> row = new HashMap<>();
        
        Double sum = 0.0;
        for (int i = 0; i < levels.size(); i++) {
            level = levels.get(i);
            Double time = (Double) registry.get(accKey + (idStrucure != null ? idStrucure : "") + level.getId());
            row.put(String.valueOf(i + startColumn), time != null ? String.valueOf(Math.round((time / divisor)*100.0)/100.0) : "");
            sum += (time != null ? time : 0) / divisor;
        }
    
        if (hasResume){
            row.put(String.valueOf(levels.size() + startColumn), String.valueOf(Math.round((sum)*100.0)/100.0));
        }

        for (int j = 1; j < totalColumns; j++) {
            items.add(CellPOI.builder().value(row.get(String.valueOf(j))).build());
        }
    
        return CellPOI.builder().value(label)
            .children(CellPOI.createSiblings(items))
            .style(Style.builder().backgroundColorRGB(backgroundColorRGB).build()).build();
    }
    
    private BlockPOI buildConsolidatedReportHead(Position position){
        List<CellPOI> items = new ArrayList<>();
        List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
        registry.put("startColumn", 1);
        registry.put("totalColumns", levels.size() + 2);

        items.add(CellPOI.builder().value("DEPENDENCIA").build());
        List<CellPOI> levelHeads = new ArrayList<>();
        for (NivelEntity e : levels){
            levelHeads.add(CellPOI.builder().value(e.getDescripcion()).style(Style.builder().width(20).build()).build());
        }
        items.add(CellPOI.builder().value("NÚMERO DE EMPLEOS DISTRIBUIDOS POR NIVELES Y DENOMINACIONES DE EMPLEOS")
                         .style(Style.builder().backgroundColorRGB(GREEN).build())
                         .children(levelHeads).build());
        items.add(CellPOI.builder().value("TOTAL DE EMPLEOS").style(Style.builder().width(20).build()).build());
        BlockPOI block = BlockPOI.builder()
            .position(position)
            .items(items)
            .style(
                Style.builder().backgroundColorRGB(BLUE).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER)
                    .bold(true).borderStyle(BorderStyle.THIN).wrapText(true).width(40).build())
            .build();
        return block;
    }

    private BlockPOI buildConsolidatedReportBody(List<Long> structureIds, Position position){
        List<CellPOI> items = new ArrayList<>();
        List<EstructuraEntity> plainedStructures = (List<EstructuraEntity>) registry.get("plainedStructures");
        if(plainedStructures != null && plainedStructures.size() > 0){
            for (EstructuraEntity e : plainedStructures){
                items.add(createTotalCell(e.getNombre(), "totalTimePerDependency_", e.getId(), HOURS_PER_MONTH, AQUA, true));
            }
        }
        return BlockPOI.builder()
            .position(position)
            .items(items)
            .showInColumn(true)
            .noExtend(true)
            .style(Style.builder().backgroundColorRGB(WHITE).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER)
                .borderStyle(BorderStyle.THIN).build())
            .build();
    }

    private BlockPOI buildConsolidatedReportResume(Position position){
        List<CellPOI> items = List.of(
            createTotalCell("PERSONAL TOTAL REQUERIDO", "totalTime_", null, HOURS_PER_MONTH, PINK, true)
        );
        return BlockPOI.builder()
            .showInColumn(true)
            .position(position)
            .items(items)
            .style(Style.builder().patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER).borderStyle(BorderStyle.THIN).bold(true).build())
            .build();
    }

    
    private void filterAndPlainByTypologyId(List<EstructuraEntity> structures, List<EstructuraEntity> plainedStructures, Long typologyId){
        for (EstructuraEntity e : structures) {
            if (e.getIdTipologia() == typologyId && e.getSubEstructuras() != null ){
                if(e.getSubEstructuras().stream().anyMatch(o -> o.getIdTipologia() != typologyId)){
                    plainedStructures.add(e);
                }
                filterAndPlainByTypologyId(e.getSubEstructuras(), plainedStructures, typologyId);
            }
        }
    }

    private void filterDistinctOfTypologyId(List<EstructuraEntity> plainedStructures, Long typologyId){
        for (EstructuraEntity e : plainedStructures) {
            e.setSubEstructuras(
                e.getSubEstructuras().stream()
                    .filter(o -> o.getIdTipologia() != typologyId)
                    .collect(Collectors.toList())
            );
        }
    }

    private String getLevelNomenclature(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        String[] words = str.split(" ");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
        } else {
            String firstPart = words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
            String secondPart = words[1].substring(0, 1).toUpperCase();
            return firstPart + ". " + secondPart + ".";
        }
    }
    
}
