package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ReportChartDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ReportStructureDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.StaticResourceMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Trace;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.PropertyComparator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Corporate;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jasperReport.ReportJR;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@RequiredArgsConstructor
@Service
public class StructureReportPDF {
    private final EstructuraService estructuraService;
    private final TipologiaService tipologiaService;
    private final ReportJR reportJR;
    private final NivelService nivelService;
    private final StaticResourceMediator staticResourceMediator;

    private Map<String, Object> registry;
    private Double HOURS_PER_MONTH;
    private String LOGO_URL = "reports/images/logo-without-name.png";
    private String HEADER_IMAGE_URL = "reports/images/header.png";
    private String INFO_ICON_URL = "reports/images/info-icon.png";
    private String CORPORATE_NAME = "Universidad Distrital Francisco José de Caldas";

    public byte[] generate(List<Long> structureIds) throws JRException, CiadtiException{
        registry = new HashMap<>();
        HOURS_PER_MONTH = Corporate.MONTHLY_WORKING_TIME.getValue();
        
        byte[] logo = staticResourceMediator.getResourceBytes(this.LOGO_URL);
        byte[] headerImage = staticResourceMediator.getResourceBytes(this.HEADER_IMAGE_URL);
        byte[] infoIcon = staticResourceMediator.getResourceBytes(this.INFO_ICON_URL);

        String filePath = "reports/structures/Structures.jrxml";
        String filePathChart = "reports/structures/Charts.jrxml";
        String filePathBlobalChart = "reports/structures/GlobalCharts.jrxml";

        List<NivelEntity> levels = nivelService.findAll();
        PropertyComparator<NivelEntity> propertyComparator = new PropertyComparator<>("id", true);
        Collections.sort(levels, propertyComparator);

        TipologiaEntity tipologiaEntity = tipologiaService.findFirstTipology();
        List<EstructuraEntity> structures = structureIds != null && structureIds.size() > 0 
                                            ? estructuraService.findAllFilteredByIds(structureIds) 
                                            : estructuraService.findAllFilteredBy(EstructuraEntity.builder().nombre("").build());
        registry.put("levels", levels);
        registry.put("structures", structures.stream().map(e -> {
            try {
                return e.clone();
            } catch (CloneNotSupportedException ex) {
                Trace.logError(this.getClass().getName(), Methods.getCurrentMethodName(this.getClass()), ex);
                return null;
            }
        }).toList());

        List<EstructuraEntity> plainedStructures = new ArrayList<>();
        filterAndPlainByIdTypology(structures, plainedStructures, tipologiaEntity.getId());
        filterDistinctOfIdTypology(plainedStructures, tipologiaEntity.getId());

        List<ReportStructureDTO> structureData = new ArrayList<>();
        buildStructureData(plainedStructures, new String[]{"", "", "", ""}, 0, structureData);
        filterStructureData(structureData);

        Double requiredTotalHours = getRequiredTotalHours(structureData);
        Double requiredTotalPeople = requiredTotalHours / HOURS_PER_MONTH;

        Map<String, Object> parameters = new HashMap<String, Object>();
        Map<String, Object> chartParameters = new HashMap<String, Object>(); 
        JRBeanCollectionDataSource structureDataSource = new JRBeanCollectionDataSource(structureData);
        chartParameters.put("chartPieDataset",  new JRBeanCollectionDataSource(getChartPieData(structureData)));
        chartParameters.put("chartPieGlobalDataset",  new JRBeanCollectionDataSource(getChartPieGlobalData(structureData)));
        chartParameters.put("chartBarHoursDataset",  new JRBeanCollectionDataSource(getChartBarData(structureData)));
        chartParameters.put("chartBarPeopleDataset",  new JRBeanCollectionDataSource(getChartBarData(structureData)));
        chartParameters.put("headerImage", new ByteArrayInputStream(headerImage));
        chartParameters.put("infoIcon", new ByteArrayInputStream(infoIcon));
        chartParameters.put("requiredTotalHours", requiredTotalHours);
        chartParameters.put("requiredTotalPeople", requiredTotalPeople);

        JasperReport chartReport = JasperCompileManager.compileReport(getClass().getClassLoader().getResourceAsStream(structureIds != null && structureIds.size() > 0 ? filePathChart : filePathBlobalChart));

        parameters.put("chartReport", chartReport);
        parameters.put("chartParameter", chartParameters);
        parameters.put("logo", new ByteArrayInputStream(logo));
        parameters.put("headerImage", new ByteArrayInputStream(headerImage));
        parameters.put("corporateName", CORPORATE_NAME);
        parameters.put("hoursPerMonth", HOURS_PER_MONTH);
        parameters.put("requiredTotalHours", requiredTotalHours);
        parameters.put("requiredTotalPeople", requiredTotalPeople);
        parameters.put("levels", levels.stream().map(e -> getLevelNomenclature(e.getDescripcion())).toList());

        return reportJR.converterToPDF(parameters, structureDataSource, filePath);
    }

    private Double getRequiredTotalHours(List<ReportStructureDTO> structureData) {
        Double total = 0.0;
        if (structureData != null){
            for(ReportStructureDTO r : structureData){
                if(r.getTiemposPorNivel() != null){
                    for (Double lv : r.getTiemposPorNivel()){
                        total += lv != null ? lv : 0.0;
                    }
                }
            }
        }
        return total;
    }
    
    
    
    

    private List<ReportChartDTO> getChartPieData(List<ReportStructureDTO> structureData) {
        List<ReportStructureDTO> structureDataCopy = new ArrayList<>(structureData);
        Collections.sort(structureDataCopy, new PropertyComparator<>("dependencia", true));
        List<ReportChartDTO> list = new ArrayList<>();
        String structureName = null;
        ReportChartDTO  reportChartDTO = null;
        for (ReportStructureDTO e : structureDataCopy) {
            if (!e.getDependencia().equals(structureName)){
                structureName = e.getDependencia();
                reportChartDTO = ReportChartDTO.builder().nombre(structureName).valor(0.0).horasPorMes(HOURS_PER_MONTH).build();
                list.add(reportChartDTO);
            }
            reportChartDTO.setValor( reportChartDTO.getValor() + (
                e.getTiemposPorNivel() != null && !e.getTiemposPorNivel().isEmpty() 
                        ? e.getTiemposPorNivel().stream().reduce(0.0, (subtotal, elemento) -> subtotal + (elemento != null ? elemento : 0.0)) 
                        : 0.0
            ));
        }
        return list;
    }

    private List<ReportChartDTO> getChartPieGlobalData(List<ReportStructureDTO> structureData) {
        List<ReportStructureDTO> structureDataCopy = new ArrayList<>(structureData);
        Collections.sort(structureDataCopy, new PropertyComparator<>("nivel", true));
        List<ReportChartDTO> list = new ArrayList<>();
        String nivelName = null;
        ReportChartDTO  reportChartDTO = null;
        for (ReportStructureDTO e : structureDataCopy) {
            if (e.getNivel() != null && !e.getNivel().isEmpty()){
                if (!e.getNivel().equals(nivelName)){
                    nivelName = e.getNivel();
                    reportChartDTO = ReportChartDTO.builder().nombre(nivelName).valor(0.0).horasPorMes(HOURS_PER_MONTH).build();
                    list.add(reportChartDTO);
                }
                reportChartDTO.setValor( reportChartDTO.getValor() + (
                    e.getTiemposPorNivel() != null && !e.getTiemposPorNivel().isEmpty() 
                            ? e.getTiemposPorNivel().stream().reduce(0.0, (subtotal, elemento) -> subtotal + (elemento != null ? elemento : 0.0)) 
                            : 0.0
                ));
            }
        }
        return list;
    }


    private List<ReportChartDTO> getChartBarData(List<ReportStructureDTO> structureData) {
        List<ReportStructureDTO> structureDataCopy = new ArrayList<>(structureData);
        Collections.sort(structureDataCopy, new PropertyComparator<>("dependencia", true));
        List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
        List<ReportChartDTO> list = new ArrayList<>();
        String structureName = null;
        List<ReportChartDTO>  group = null;
        for (ReportStructureDTO e : structureDataCopy) {
            if (!e.getDependencia().equals(structureName)){
                structureName = e.getDependencia();
                group = new ArrayList<>();
                for(NivelEntity levl : levels){
                    group.add(ReportChartDTO.builder().nombre(structureName).nivel(levl.getDescripcion()).valor(0.0).horasPorMes(HOURS_PER_MONTH).build());
                }
                list.addAll(group);
            }
            for(int i=0; i<group.size(); i++){
                ReportChartDTO reportChartDTO = group.get(i);
                reportChartDTO.setValor( reportChartDTO.getValor() + (
                    e.getTiemposPorNivel() != null && !e.getTiemposPorNivel().isEmpty() 
                    ? ( e.getTiemposPorNivel().get(i)  != null ?  e.getTiemposPorNivel().get(i)  : 0.0)
                    : 0.0
                ));
            }
        }
        return list;
    }

    private String getInformation(EstructuraEntity structure){
        if (structure.getSubEstructuras() != null && !structure.getSubEstructuras().isEmpty()){
            boolean onlyHasChildrenOfSameType = !structure.getSubEstructuras().stream().anyMatch(e -> e.getIdTipologia() != structure.getIdTipologia());
            if (onlyHasChildrenOfSameType){
                return null;
            }
        }
        return getNameRecursive(structure);
    }

    private String getNameRecursive(EstructuraEntity structure){
        List<EstructuraEntity> structures = (List<EstructuraEntity>)registry.get("structures"); 
        EstructuraEntity parent = findStructure(structure.getIdPadre(), structures);
        if(parent != null && parent.getIdTipologia() == structure.getIdTipologia()){
            return getNameRecursive(parent) + " - " + structure.getNombre();
        }
        return structure.getNombre();
    }

    private EstructuraEntity findStructure(Long id, List<EstructuraEntity> structures){
        if (structures != null){
            for (EstructuraEntity e : structures){
                if(e.getId().equals(id)){ 
                    return e; 
                }else{
                    EstructuraEntity obj = findStructure(id, e.getSubEstructuras());
                    if (obj != null){
                        return obj;
                    }
                }
            }
        }
        return null;
    }

    private void buildStructureData(List<EstructuraEntity> structures, String[] tipologyStructures, int level, List<ReportStructureDTO>  results){
        for (EstructuraEntity structure : structures){
            tipologyStructures[level] =  getInformation(structure);
            if (structure.getSubEstructuras() != null && !structure.getSubEstructuras().isEmpty()){
                boolean onlyHasChildrenOfSameType = !structure.getSubEstructuras().stream().anyMatch(e -> e.getIdTipologia() != structure.getIdTipologia());
                if(onlyHasChildrenOfSameType){
                    buildStructureData(structure.getSubEstructuras(), tipologyStructures, level, results);
                }else{
                    buildStructureData(structure.getSubEstructuras(), tipologyStructures, level + 1, results);
                }
            }else{
                Double frecuency = null;
                String levelNomenclature = null;
                String levl = null;
                Double minTime = null;
                Double meanTime = null;
                Double maxTime = null;
                Double standarTime = null;
                List<Double> tiemposPorNivel = new ArrayList<>();

                if (structure.getActividad() != null){
                    List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
                    levelNomenclature = getLevelNomenclature(structure.getActividad().getNivel().getDescripcion());
                    levl = String.format("%s (%s)", structure.getActividad().getNivel().getDescripcion(), levelNomenclature);
                    frecuency = structure.getActividad().getFrecuencia();
                    meanTime = (double) Math.round((structure.getActividad().getTiempoPromedio() / 60.0)*100)/100;
                    minTime = (double) Math.round((structure.getActividad().getTiempoMinimo() / 60.0)*100)/100;
                    maxTime = (double) Math.round((structure.getActividad().getTiempoMaximo() / 60.0)*100)/100;
                    standarTime = (double) Math.round((1.07*(minTime + 4*meanTime + maxTime)/6)*100) /100;
                    for (NivelEntity e : levels){
                        tiemposPorNivel.add(e.getId() == structure.getActividad().getIdNivel() ? Math.round((frecuency * standarTime)*10)/10.0 : null);
                    }
                }
                ReportStructureDTO reportDTO = ReportStructureDTO.builder()
                .dependencia(level >= 0 ? tipologyStructures[0] : "")
                .proceso(level >= 1 ? tipologyStructures[1] : "")
                .procedimiento(level >= 2 ? tipologyStructures[2] : "")
                .actividad(level >= 3 ? tipologyStructures[3] : "")
                .nivel(levl != null ? levl : "")
                .frecuencia(frecuency)
                .tiempoMinimo(minTime)
                .tiempoPromedio(meanTime)
                .tiempoMaximo(maxTime)
                .tiempoEstandar(standarTime)
                .tiemposPorNivel(tiemposPorNivel)
                .build();
                results.add(reportDTO);
            }
        }
    }

    private void filterStructureData(List<ReportStructureDTO> list){
        String procedimiento = null;
        for (ReportStructureDTO reportDTO  : list){
            if (reportDTO.getProcedimiento().equals(procedimiento)){
                reportDTO.setProcedimiento("");
            }else{
                procedimiento = reportDTO.getProcedimiento();
            }
        }
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
