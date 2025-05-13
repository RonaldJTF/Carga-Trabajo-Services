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
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
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
public class OperationalManagementReportPDF {
    private final GestionOperativaService gestionOperativaService;
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

    public byte[] generate(List<Long> operationalManagementIds) throws JRException, CiadtiException{
        registry = new HashMap<>();
        HOURS_PER_MONTH = Corporate.MONTHLY_WORKING_TIME.getValue();
        
        byte[] logo = staticResourceMediator.getResourceBytes(this.LOGO_URL);
        byte[] headerImage = staticResourceMediator.getResourceBytes(this.HEADER_IMAGE_URL);
        byte[] infoIcon = staticResourceMediator.getResourceBytes(this.INFO_ICON_URL);

        String filePath = "reports/operationalsManagements/OperationalsManagements.jrxml";
        String filePathChart = "reports/operationalsManagements/OperationalsManagementsChart.jrxml";

        List<NivelEntity> levels = nivelService.findAllInSomeActivity();

        TipologiaEntity firstTipology = tipologiaService.findFirstTipology();
        TipologiaEntity secondTipology = tipologiaService.findById(firstTipology.getIdTipologiaSiguiente());

        List<GestionOperativaEntity> operationalsManagements = operationalManagementIds != null && operationalManagementIds.size() > 0 
                                            ? gestionOperativaService.findAllFilteredByIds(operationalManagementIds) 
                                            : gestionOperativaService.findAllFilteredBy(GestionOperativaEntity.builder().nombre("").build());
        registry.put("levels", levels);
        registry.put("operationalsManagements", operationalsManagements.stream().map(e -> {
            try {
                return e.clone();
            } catch (CloneNotSupportedException ex) {
                Trace.logError(this.getClass().getName(), Methods.getCurrentMethodName(this.getClass()), ex);
                return null;
            }
        }).toList());

        List<GestionOperativaEntity> plainedOperationalsManagements = new ArrayList<>();
        filterAndPlainByTypologyId(operationalsManagements, plainedOperationalsManagements, secondTipology.getId());
        filterDistinctOfTypologyId(plainedOperationalsManagements, secondTipology.getId());

        List<ReportStructureDTO> operationalsManagementsData = new ArrayList<>();
        buildOperationalManagementData(plainedOperationalsManagements, new String[]{"", "", ""}, 0, operationalsManagementsData);

        Double requiredTotalHours = getRequiredTotalHours(operationalsManagementsData);
        Double requiredTotalPeople = requiredTotalHours / HOURS_PER_MONTH;

        Map<String, Object> parameters = new HashMap<String, Object>();
        Map<String, Object> chartParameters = new HashMap<String, Object>(); 
        JRBeanCollectionDataSource structureDataSource = new JRBeanCollectionDataSource(operationalsManagementsData);
        chartParameters.put("chartPieDataset",  new JRBeanCollectionDataSource(getChartPieData(operationalsManagementsData)));
        chartParameters.put("chartPieGlobalDataset",  new JRBeanCollectionDataSource(getChartPieGlobalData(operationalsManagementsData)));
        chartParameters.put("chartBarHoursDataset",  new JRBeanCollectionDataSource(getChartBarData(operationalsManagementsData)));
        chartParameters.put("chartBarPeopleDataset",  new JRBeanCollectionDataSource(getChartBarData(operationalsManagementsData)));
        chartParameters.put("headerImage", new ByteArrayInputStream(headerImage));
        chartParameters.put("infoIcon", new ByteArrayInputStream(infoIcon));
        chartParameters.put("requiredTotalHours", requiredTotalHours);
        chartParameters.put("requiredTotalPeople", requiredTotalPeople);

        if(operationalManagementIds != null && operationalManagementIds.size() > 0){
            JasperReport chartReport = JasperCompileManager.compileReport(getClass().getClassLoader().getResourceAsStream(filePathChart));
            parameters.put("chartReport", chartReport);
        }
        
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

    private Double getRequiredTotalHours(List<ReportStructureDTO> operationalsManagementsData) {
        return operationalsManagementsData == null ? 0.0 : 
        operationalsManagementsData.stream()
                .filter(r -> r.getTiemposPorNivel() != null)
                .flatMap(r -> r.getTiemposPorNivel().stream())
                .mapToDouble(lv -> lv != null ? lv : 0.0)
                .sum();
    }
    

    private List<ReportChartDTO> getChartPieData(List<ReportStructureDTO> operationalsManagementsData) {
        List<ReportStructureDTO> operationalsManagementsDataCopy = new ArrayList<>(operationalsManagementsData);
        Collections.sort(operationalsManagementsDataCopy, new PropertyComparator<>("proceso", true));
        List<ReportChartDTO> list = new ArrayList<>();
        String processName = null;
        ReportChartDTO  reportChartDTO = null;
        for (ReportStructureDTO e : operationalsManagementsDataCopy) {
            if (!e.getProceso().equals(processName)){
                processName = e.getProceso();
                reportChartDTO = ReportChartDTO.builder().nombre(processName).valor(0.0).horasPorMes(HOURS_PER_MONTH).build();
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

    private List<ReportChartDTO> getChartPieGlobalData(List<ReportStructureDTO> operationalsManagementsData) {
        List<ReportStructureDTO> operationalsManagementsDataCopy = new ArrayList<>(operationalsManagementsData);
        Collections.sort(operationalsManagementsDataCopy, new PropertyComparator<>("nivel", true));
        List<ReportChartDTO> list = new ArrayList<>();
        String nivelName = null;
        ReportChartDTO  reportChartDTO = null;
        for (ReportStructureDTO e : operationalsManagementsDataCopy) {
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

    private List<ReportChartDTO> getChartBarData(List<ReportStructureDTO> operationalsManagementsData) {
        List<ReportStructureDTO> operationalsManagementsDataCopy = new ArrayList<>(operationalsManagementsData);
        Collections.sort(operationalsManagementsDataCopy, new PropertyComparator<>("proceso", true));
        List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
        List<ReportChartDTO> list = new ArrayList<>();
        String structureName = null;
        List<ReportChartDTO>  group = null;
        for (ReportStructureDTO e : operationalsManagementsDataCopy) {
            if (!e.getProceso().equals(structureName)){
                structureName = e.getProceso();
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

    private String getInformation(GestionOperativaEntity operationalManagement){
        if (operationalManagement.getSubGestionesOperativas() != null && !operationalManagement.getSubGestionesOperativas().isEmpty()){
            boolean onlyHasChildrenOfSameType = !operationalManagement.getSubGestionesOperativas().stream().anyMatch(e -> e.getIdTipologia() != operationalManagement.getIdTipologia());
            if (onlyHasChildrenOfSameType){
                return null;
            }
        }
        return getNameRecursive(operationalManagement);
    }

    private String getNameRecursive(GestionOperativaEntity operationalManagement){
        List<GestionOperativaEntity> operationalsManagements = (List<GestionOperativaEntity>)registry.get("operationalsManagements"); 
        GestionOperativaEntity parent = findOperationalManagement(operationalManagement.getIdPadre(), operationalsManagements);
        if(parent != null && parent.getIdTipologia() == operationalManagement.getIdTipologia()){
            return getNameRecursive(parent) + " - " + operationalManagement.getNombre();
        }
        return operationalManagement.getNombre();
    }

    private GestionOperativaEntity findOperationalManagement(Long id, List<GestionOperativaEntity> operationalsManagements){
        if (operationalsManagements != null){
            for (GestionOperativaEntity e : operationalsManagements){
                if(e.getId().equals(id)){ 
                    return e; 
                }else{
                    GestionOperativaEntity obj = findOperationalManagement(id, e.getSubGestionesOperativas());
                    if (obj != null){
                        return obj;
                    }
                }
            }
        }
        return null;
    }

    private void buildOperationalManagementData(List<GestionOperativaEntity> operationalsManagements, String[] tipologyOperationalManagement, int level, List<ReportStructureDTO>  results){
        for (GestionOperativaEntity operationalManagement : operationalsManagements){
            tipologyOperationalManagement[level] =  getInformation(operationalManagement);
            if (operationalManagement.getSubGestionesOperativas() != null && !operationalManagement.getSubGestionesOperativas().isEmpty()){
                boolean onlyHasChildrenOfSameType = !operationalManagement.getSubGestionesOperativas().stream().anyMatch(e -> e.getIdTipologia() != operationalManagement.getIdTipologia());
                if(onlyHasChildrenOfSameType){
                    buildOperationalManagementData(operationalManagement.getSubGestionesOperativas(), tipologyOperationalManagement, level, results);
                }else{
                    buildOperationalManagementData(operationalManagement.getSubGestionesOperativas(), tipologyOperationalManagement, level + 1, results);
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

                if (operationalManagement.getActividad() != null){
                    List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
                    levelNomenclature = getLevelNomenclature(operationalManagement.getActividad().getNivel().getDescripcion());
                    levl = String.format("%s (%s)", operationalManagement.getActividad().getNivel().getDescripcion(), levelNomenclature);
                    frecuency = operationalManagement.getActividad().getFrecuencia();
                    meanTime = (double) Math.round((operationalManagement.getActividad().getTiempoPromedio() / 60.0)*100)/100;
                    minTime = (double) Math.round((operationalManagement.getActividad().getTiempoMinimo() / 60.0)*100)/100;
                    maxTime = (double) Math.round((operationalManagement.getActividad().getTiempoMaximo() / 60.0)*100)/100;
                    standarTime = (double) Math.round((1.07*(minTime + 4*meanTime + maxTime)/6)*100) /100;
                    for (NivelEntity e : levels){
                        tiemposPorNivel.add(e.getId() == operationalManagement.getActividad().getIdNivel() ? Math.round((frecuency * standarTime)*100)/100.0 : null);
                    }
                }
                ReportStructureDTO reportDTO = ReportStructureDTO.builder()
                    .proceso(level >= 0 ? tipologyOperationalManagement[0] : "")
                    .procedimiento(level >= 1 ? tipologyOperationalManagement[1] : "")
                    .actividad(level >= 2 ? tipologyOperationalManagement[2] : "")
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

    private void filterAndPlainByTypologyId(List<GestionOperativaEntity> operationalsManagements, List<GestionOperativaEntity> plainedOperationalsManagements, Long typologyId) {
        for (GestionOperativaEntity e : operationalsManagements) {
            if (e.getIdTipologia() == typologyId && e.getSubGestionesOperativas() != null) {
                if (e.getSubGestionesOperativas().stream().anyMatch(o -> o.getIdTipologia() != typologyId)) {
                    plainedOperationalsManagements.add(e);
                }
                filterAndPlainByTypologyId(e.getSubGestionesOperativas(), plainedOperationalsManagements, typologyId);
            }
        }
    }

    private void filterDistinctOfTypologyId(List<GestionOperativaEntity> plainedOperationalsManagements, Long typologyId){
        for (GestionOperativaEntity e : plainedOperationalsManagements) {
            e.setSubGestionesOperativas(
                e.getSubGestionesOperativas().stream()
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
