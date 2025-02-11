package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
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
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Corporate;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jasperReport.ReportJR;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@RequiredArgsConstructor
@Service
public class StructureTimeStatisticsReportPDF {
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

    private String REPORT_MAIN_PATH = "reports/structures/StructureTimeStatistics.jrxml";
    private String REPORT_STRUCTURE_DETAIL_PATH = "reports/structures/StructureDetail.jrxml";

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataSource {
        private Map<String, Object> parameters;
        private JasperReport report;
        private JRBeanCollectionDataSource resource;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StructureData {
        private String nombre;
        private String nivel;
        private Double valor;
        private List<Double> tiemposPorNivel;
    }

    public byte[] generate(Long structureId) throws JRException, CiadtiException{
        registry = new HashMap<>();
        HOURS_PER_MONTH = Corporate.MONTHLY_WORKING_TIME.getValue();
        
        byte[] logo = staticResourceMediator.getResourceBytes(this.LOGO_URL);
        byte[] headerImage = staticResourceMediator.getResourceBytes(this.HEADER_IMAGE_URL);

        List<NivelEntity> levels = nivelService.findAllInSomeActivity();

        List<EstructuraEntity> structures = estructuraService.findAllFilteredBy(EstructuraEntity.builder().id(structureId).build());
        EstructuraEntity structureWorkingOn = structures.get(0);
        TipologiaEntity typology = tipologiaService.findWithNextTipologyHierarchicallyById(structureWorkingOn.getIdTipologia());
        
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
        filterAndPlainByTypologyId(structures, plainedStructures, typology.getId());
        filterDistinctOfTypologyId(plainedStructures, typology.getId());

        List<ReportStructureDTO> structureData = new ArrayList<>();
        buildStructureData(plainedStructures, new HashMap<>(), structureData);
        
        Double requiredTotalHours = getRequiredTotalHours(structureData);
        Double requiredTotalPeople = requiredTotalHours / HOURS_PER_MONTH;

        Map<String, Object> parameters = new HashMap<String, Object>();
        List<DataSource> datasources = new ArrayList<>();
        TipologiaEntity t = typology;
        while (t != null){
            datasources.add(buildDataSource(structureData, t.getId()));
            t = t.getTipologiaSiguiente();
        }
        parameters.put("logo", new ByteArrayInputStream(logo));
        parameters.put("headerImage", new ByteArrayInputStream(headerImage));
        parameters.put("corporateName", CORPORATE_NAME);
        parameters.put("hoursPerMonth", HOURS_PER_MONTH);
        parameters.put("requiredTotalHours", requiredTotalHours);
        parameters.put("requiredTotalPeople", requiredTotalPeople);
        JRBeanCollectionDataSource structureDataSource = new JRBeanCollectionDataSource(datasources);
        return reportJR.converterToPDF(parameters, structureDataSource, REPORT_MAIN_PATH);
    }

    @SuppressWarnings({ "unchecked", "null" })
    private DataSource buildDataSource(List<ReportStructureDTO> reportStructures, Long typologyId) throws JRException, CiadtiException{
        JasperReport report = JasperCompileManager.compileReport(getClass().getClassLoader().getResourceAsStream(REPORT_STRUCTURE_DETAIL_PATH));
        Map<String, Object> parameters = new HashMap<String, Object>(); 
        
        String typologyName = tipologiaService.findById(typologyId).getNombre();
        List<StructureData> data = new ArrayList<>();
        String tempStructure = "";
        StructureData structureData = null;
        Double timeValue;
        String name;
        for(ReportStructureDTO e : reportStructures){
            name = e.getNombrePorTipologia().get(typologyId);
            if(!tempStructure.equals(name)){
                structureData = StructureData.builder().nombre(name).tiemposPorNivel(new ArrayList<>()).build();
                data.add(structureData);
                tempStructure =  name != null ? name : "";
            }
            for(int i=0; i<e.getTiemposPorNivel().size(); i++){
                timeValue = e.getTiemposPorNivel().get(i) != null ?  e.getTiemposPorNivel().get(i) : 0.0;
                if(structureData.getTiemposPorNivel().size() > i){
                    structureData.getTiemposPorNivel().set(i, structureData.getTiemposPorNivel().get(i) + timeValue);
                }else{
                    structureData.getTiemposPorNivel().add(timeValue);
                }
            }
        }
        data.sort((d1, d2) -> {
            double suma1 = d1.getTiemposPorNivel().stream().mapToDouble(Double::doubleValue).sum();
            double suma2 = d2.getTiemposPorNivel().stream().mapToDouble(Double::doubleValue).sum();
            return Double.compare(suma2, suma1);
        });

        parameters.put("levels", ((List<NivelEntity>)registry.get("levels")).stream().map(e -> e.getDescripcion()).toList());
        parameters.put("structureName", typologyName);
        parameters.put("chartBarHoursDataset",  new JRBeanCollectionDataSource(getChartBarData(data)));
        return DataSource.builder().report(report).parameters(parameters).resource(new JRBeanCollectionDataSource(data)).build();
    }


    private List<ReportChartDTO> getChartBarData(List<StructureData> structureData) {
        List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
        List<ReportChartDTO> list = new ArrayList<>();
        NivelEntity levl;
        for (StructureData e : structureData) {
            for(int i=0; i<levels.size(); i++){
                levl = levels.get(i);
                list.add(ReportChartDTO
                    .builder()
                    .nombre(e.getNombre())
                    .nivel(levl.getDescripcion())
                    .valor(e.getTiemposPorNivel() != null && !e.getTiemposPorNivel().isEmpty() ? e.getTiemposPorNivel().get(i) : 0.0)
                    .horasPorMes(HOURS_PER_MONTH).build()
                );
            }
        }
        return list;
    }


    private Double getRequiredTotalHours(List<ReportStructureDTO> structureData) {
        return structureData == null ? 0.0 : 
            structureData.stream()
                .filter(r -> r.getTiemposPorNivel() != null)
                .flatMap(r -> r.getTiemposPorNivel().stream())
                .mapToDouble(lv -> lv != null ? lv : 0.0)
                .sum();
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
        @SuppressWarnings("unchecked")
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

    private void buildStructureData(List<EstructuraEntity> structures, Map<Long, String> nameByTypology, List<ReportStructureDTO>  results){
        for (EstructuraEntity structure : structures){
            nameByTypology.put(structure.getIdTipologia(), getInformation(structure));
            if (structure.getSubEstructuras() != null && !structure.getSubEstructuras().isEmpty()){
                buildStructureData(structure.getSubEstructuras(), nameByTypology, results);
            }else{
                Double frecuency = null;
                String levl = null;
                Double minTime = null;
                Double meanTime = null;
                Double maxTime = null;
                Double standarTime = null;
                List<Double> tiemposPorNivel = new ArrayList<>();

                if (structure.getActividad() != null){
                    @SuppressWarnings("unchecked")
                    List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
                    levl = structure.getActividad().getNivel().getDescripcion();
                    frecuency = structure.getActividad().getFrecuencia();
                    meanTime = (double) Math.round((structure.getActividad().getTiempoPromedio() / 60.0)*100)/100;
                    minTime = (double) Math.round((structure.getActividad().getTiempoMinimo() / 60.0)*100)/100;
                    maxTime = (double) Math.round((structure.getActividad().getTiempoMaximo() / 60.0)*100)/100;
                    standarTime = (double) Math.round((1.07*(minTime + 4*meanTime + maxTime)/6)*100) /100;
                    for (NivelEntity e : levels){
                        tiemposPorNivel.add(e.getId() == structure.getActividad().getIdNivel() ? frecuency * standarTime : null);
                    }
                }
                ReportStructureDTO reportDTO = ReportStructureDTO.builder()
                    .nombrePorTipologia(new HashMap<>(nameByTypology))
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

    private void filterAndPlainByTypologyId(List<EstructuraEntity> structures, List<EstructuraEntity> plainedStructures, Long typologyId){
        for (EstructuraEntity e : structures) {
            if (e.getIdTipologia() == typologyId && e.getSubEstructuras() != null && !e.getSubEstructuras().isEmpty()){
                if(e.getSubEstructuras().stream().anyMatch(o -> o.getIdTipologia() != typologyId)){
                    plainedStructures.add(e);
                }
                filterAndPlainByTypologyId(e.getSubEstructuras(), plainedStructures, typologyId);
            }else if(e.getIdTipologia() == typologyId){
                plainedStructures.add(e);
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
}
