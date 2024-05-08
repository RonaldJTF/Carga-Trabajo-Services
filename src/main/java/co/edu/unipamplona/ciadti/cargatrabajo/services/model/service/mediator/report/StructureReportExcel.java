package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.BlockPOI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.CellPOI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.Position;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.ReportPOI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.Style;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class StructureReportExcel {
    private final EstructuraService estructuraService;
    private final TipologiaService tipologiaService;
    private final NivelService nivelService;
    private final ResourceLoader resourceLoader;

    private final Map<String, Object> registry = new HashMap<>();

    private final int[] GRAY = {242, 242, 242};
    private final int[] GREEN = {238, 245, 208};
    private final int[] WHITE = {255, 255, 255};
    private final int[] BLUE = {208, 224, 245};
    private final int[] AQUA = {233, 255, 253};
    private final int[] PINK = {245, 212, 208};

    private final Boolean[] BORDER_TOP = {true, false, false, false};
    private final Boolean[] BORDER_RIGHT = {false, true, false, false};
    private final Boolean[] BORDER_BOTTOM = {false, false, true, false};
    private final Boolean[] BORDER_LEFT = {false, false, false, true};

    public byte[] generate(List<Long> structureIds) throws CiadtiException{
        BlockPOI titleBlock = buildDependencyReportTitle(Position.builder().x(1).y(1).build());
        BlockPOI entityBlock = buildDependencyReportEntity(null);

        Map<String, Position> positions;
        ReportPOI report = new ReportPOI();
        positions = report.addTitle(titleBlock);
        Position bottomTitlePosition = positions.get("bottom");
        bottomTitlePosition.setY(bottomTitlePosition.getY() + 1);

        entityBlock.setPosition(bottomTitlePosition);
        positions = report.addBlock(entityBlock);
        Position bottomEntityPosition = positions.get("bottom");
        bottomEntityPosition.setY(bottomEntityPosition.getY() + 1);
        positions = report.addBlock(buildDependencyReportHead(bottomEntityPosition));

        Position TopRightHeadPosition = positions.get("top-right");
        TopRightHeadPosition.setY(TopRightHeadPosition.getY() - 1);
        TopRightHeadPosition.setX(TopRightHeadPosition.getX() - 3);
        report.addBlock(buildDependencyReportDate(TopRightHeadPosition));

        positions = report.addBlock(buildDependencyReportStructure(structureIds, positions.get("bottom")));
        positions = report.addBlock(buildDependencyReportResume(positions.get("bottom")));

        return report.generate();
    }

    private BlockPOI buildDependencyReportTitle(Position position) throws CiadtiException{
        return BlockPOI.builder()
            .position(position)
            .style(Style.builder().backgroundColorRGB(GRAY).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER).build())
            .items(List.of(
                    CellPOI.builder().value(getImageBytes()).style(Style.builder().width(20).border(BorderStyle.MEDIUM).build()).build(),
                    CellPOI.builder().value("DEPARTAMENTO ADMINISTRATIVO DE LA FUNCIÓN PÚBLICA").style(Style.builder().font("Arial Black").fontSize(14).borderStyle(BorderStyle.MEDIUM).borders(new Boolean[]{true, true, false, false}).build())
                        .children(CellPOI.createSiblings(List.of(
                            CellPOI.builder().value("DIRECCIÓN DE DESARROLLO ORGANIZACIONAL").style(Style.builder().fontSize(12).borders(BORDER_RIGHT).build()).build(),
                            CellPOI.builder().value("MEDICIÓN DE CARGAS DE TRABAJO POR DEPENDENCIA").style(Style.builder().bold(true).font("Calibri").borders(BORDER_RIGHT).build()).build(),
                            CellPOI.builder().value("FORMULARIO 1").style(Style.builder().borders(new Boolean[]{false, true, true, false}).build()).build()
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
        }
        return null;
    }

    private BlockPOI buildDependencyReportEntity(Position position){
        return BlockPOI.builder()
            .style(Style.builder().backgroundColorRGB(GRAY).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER).border(BorderStyle.THIN).wrapText(true).build())
            .items(List.of(
                CellPOI.builder().value("ENTIDAD").build(),
                CellPOI.builder().value("ALCALDÍA DE SAN JOSÉ DE CÚCUTA").style(Style.builder().backgroundColorRGB(WHITE).build()).build()
            )).build();
    }

    private BlockPOI buildDependencyReportDate(Position position){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String date = dateFormat.format(new Date());
        return BlockPOI.builder()
            .position(position)
            .style(Style.builder().backgroundColorRGB(GRAY).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER).border(BorderStyle.THIN).wrapText(true).build())
            .items(List.of(
                CellPOI.builder().value("Fecha del reporte").aditionalCellsToUse(1).build(),
                CellPOI.builder().value(date).aditionalCellsToUse(1).build()
            )).build();
    }

    private BlockPOI buildDependencyReportHead(Position position){
        List<CellPOI> items = new ArrayList<>();
        List<TipologiaEntity> typologies = tipologiaService.findAllManagement(); 
        List<NivelEntity> levels = nivelService.findAll();
        registry.put("levels", levels);
        registry.put("typologies", typologies);
        registry.put("totalColumns", typologies.size() + levels.size() + 7);

        for (TipologiaEntity e : typologies){ items.add(CellPOI.builder().value(e.getNombre()).build());}

        List<CellPOI> levelHeads = new ArrayList<>();
        for (NivelEntity e : levels){
            levelHeads.add(CellPOI.builder().value(e.getDescripcion().substring(0, 3).toUpperCase()).build());
        }

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
                    .bold(true).border(BorderStyle.THIN).wrapText(true).width(30).build())
            .build();
        return block;
    }

    private BlockPOI buildDependencyReportStructure(List<Long> structureIds, Position position){
        List<CellPOI> items = new ArrayList<>();
        List<TipologiaEntity> typologies = (List<TipologiaEntity>)registry.get("typologies");
        TipologiaEntity tipologiaEntity = tipologiaService.findFirstTipology();
        List<EstructuraEntity> structures = structureIds != null && structureIds.size() > 0 
                                            ? estructuraService.findAllFilteredByIds(structureIds) 
                                            : estructuraService.findAllFilteredBy(EstructuraEntity.builder().nombre("").build());
        List<EstructuraEntity> plainedStructures = new ArrayList<>();
        filterAndPlainByIdTypology(structures, plainedStructures, tipologiaEntity.getId());
        filterDistinctOfIdTypology(plainedStructures, tipologiaEntity.getId());
        addItem(items, plainedStructures, 0, typologies.size());

        registry.put("structures", plainedStructures);

        return BlockPOI.builder()
            .position(position)
            .items(items)
            .showInColumn(true)
            .noExtend(true)
            .style(Style.builder().backgroundColorRGB(WHITE).patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER)
                .border(BorderStyle.THIN).build())
            .build();
    }

    private BlockPOI buildDependencyReportResume(Position position){
        List<CellPOI> items = List.of(
            createTotalCell("TOTAL HORAS REQUERIDAS", "totalTimePerDependency_", 1, PINK),
            createTotalCell("PERSONAL TOTAL REQUERIDO", "totalTimePerDependency_", 167, PINK)
        );
        return BlockPOI.builder()
            .showInColumn(true)
            .position(position)
            .items(items)
            .style(Style.builder().patternType(FillPatternType.SOLID_FOREGROUND).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER).border(BorderStyle.THIN).bold(true).build())
            .build();
    }

    private void addItem (List<CellPOI> items,  List<EstructuraEntity> structures, int deep, int maxDeep){
        if(structures != null && structures.size() > 0){
            for (EstructuraEntity structure : structures){
                CellPOI cell = CellPOI.builder().value(structure.getNombre()).children(new ArrayList<>()).build();
                items.add(cell);
                if (maxDeep - deep - 1 > 0){
                    addItem(cell.getChildren(), structure.getSubEstructuras(), deep + 1, maxDeep);
                    if ("1".equals(structure.getTipologia().getEsDependencia())){
                        items.add(createTotalCell("Horas requeridas", "temporalTotalTimePerDependency_", 1, AQUA));
                        items.add(createTotalCell("Personal requerido", "temporalTotalTimePerDependency_", 167, AQUA));
                        List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
                        for (NivelEntity e : levels){
                            registry.put("temporalTotalTimePerDependency_"+e.getId(), null);
                        }
                    }
                }else{
                    if (structure.getActividad() != null){
                        List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");

                        String levl = structure.getActividad().getNivel().getDescripcion();
                        String levelNomenclature = structure.getActividad().getNivel().getDescripcion().substring(0, 3).toUpperCase();
                        Double frecuency = structure.getActividad().getFrecuencia();
                        Double minTime = (double) Math.round((structure.getActividad().getTiempoMinimo() / 60.0)*100.0)/100.0;
                        Double meanTime = (double) Math.round((structure.getActividad().getTiempoPromedio() / 60.0)*100.0)/100.0;
                        Double maxTime = (double) Math.round((structure.getActividad().getTiempoMaximo() / 60.0)*100.0)/100.0;
                        Double standarTime = (double) Math.round((1.07*(minTime + 4*meanTime + maxTime)/6)*100.0) /100.0;

                        List<CellPOI> timeResume = new ArrayList<>();
                        for (NivelEntity e : levels){
                            timeResume.add(CellPOI.builder().value(e.getId() == structure.getActividad().getIdNivel() ? String.valueOf(Math.round((frecuency * standarTime)*100.0)/100.0) : "").build());
                             
                            Double acc = (Double)registry.get("totalTimePerDependency_"+e.getId());  
                            if (e.getId() == structure.getActividad().getIdNivel()){
                                if (acc == null){
                                   acc = 0.0;
                                } 
                                acc += frecuency * standarTime;
                                registry.put("totalTimePerDependency_"+e.getId(), acc);
                            }

                            Double accPerDependency = (Double)registry.get("temporalTotalTimePerDependency_"+e.getId());  
                            if (e.getId() == structure.getActividad().getIdNivel()){
                                if (accPerDependency == null){
                                    accPerDependency = 0.0;
                                } 
                                accPerDependency += frecuency * standarTime;
                                registry.put("temporalTotalTimePerDependency_"+e.getId(), accPerDependency);
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
        }else{
            if (maxDeep - deep > 0){
                CellPOI cell = CellPOI.builder().value("").children(new ArrayList<>()).build();
                items.add(cell);
                addItem(cell.getChildren(), null, deep + 1, maxDeep);
            }
        }
    }

    private CellPOI createTotalCell(String label, String accKey, double divisor, int[] backgroundColorRGB) {
        List<CellPOI> items = new ArrayList<>();
        List<NivelEntity> levels = (List<NivelEntity>) registry.get("levels");
        int totalColumns = (int) registry.get("totalColumns");
        NivelEntity level;
        Map<String, String> row = new HashMap<>();
        
        for (int i = 0; i < levels.size(); i++) {
            level = levels.get(i);
            Double time = (Double) registry.get(accKey + level.getId());
            row.put(String.valueOf(i + totalColumns - levels.size()), time != null ? String.valueOf(Math.round((time / divisor)*100.0)/100.0) : "");
        }
    
        for (int j = 1; j < totalColumns; j++) {
            items.add(CellPOI.builder().value(row.get(String.valueOf(j))).build());
        }
    
        return CellPOI.builder().value(label)
            .children(CellPOI.createSiblings(items))
            .style(Style.builder().backgroundColorRGB(backgroundColorRGB).build()).build();
    }

    private void filterAndPlainByIdTypology(List<EstructuraEntity> structures, List<EstructuraEntity> plainedStructures, Long idTypology){
        for (EstructuraEntity e : structures) {
            if (e.getTipologia().getId() == idTypology && e.getSubEstructuras() != null ){
                if(e.getSubEstructuras().stream().anyMatch(o -> o.getTipologia().getId() != idTypology)){
                    plainedStructures.add(e);
                }
                filterAndPlainByIdTypology(e.getSubEstructuras(), plainedStructures, idTypology);
            }
        }
    }

    private void filterDistinctOfIdTypology(List<EstructuraEntity> plainedStructures, Long idTypology){
        for (EstructuraEntity e : plainedStructures) {
            e.setSubEstructuras(
                e.getSubEstructuras().stream()
                    .filter(o -> o.getTipologia().getId() != idTypology)
                    .collect(Collectors.toList())
            );
        }
    }
}
