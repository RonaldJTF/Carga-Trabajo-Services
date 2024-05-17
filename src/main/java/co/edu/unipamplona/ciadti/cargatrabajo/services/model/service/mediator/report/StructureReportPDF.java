package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jasperReport.ReportJR;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi.CellPOI;
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
    private final ResourceLoader resourceLoader;

    private Map<String, Object> registry;
    private Double HOURS_PER_MONTH;

    public byte[] generate(List<Long> structureIds) throws JRException, CiadtiException{
        registry = new HashMap<>();
        HOURS_PER_MONTH = 167.0;

        byte[] logo = getImageBytes();
        String filePath = "reports/structures/Structures.jrxml";
        String filePathDependency = "reports/structures/Charts.jrxml";

        List<NivelEntity> levels = nivelService.findAll();
        registry.put("levels", levels);

        TipologiaEntity tipologiaEntity = tipologiaService.findFirstTipology();
        List<EstructuraEntity> structures = structureIds != null && structureIds.size() > 0 
                                            ? estructuraService.findAllFilteredByIds(structureIds) 
                                            : estructuraService.findAllFilteredBy(EstructuraEntity.builder().nombre("").build());
        List<EstructuraEntity> plainedStructures = new ArrayList<>();
        filterAndPlainByIdTypology(structures, plainedStructures, tipologiaEntity.getId());
        filterDistinctOfIdTypology(plainedStructures, tipologiaEntity.getId());

        List<ReportStructureDTO> structureData = new ArrayList<>();
        buildStructureData(plainedStructures, new String[]{"", "", "", ""}, 0, structureData);
        filterStructureData(structureData);

        Map<String, Object> parameters = new HashMap<String, Object>();
        Map<String, Object> chartParameters = new HashMap<String, Object>(); 
        JRBeanCollectionDataSource structureDataSource = new JRBeanCollectionDataSource(structureData);
        chartParameters.put("chartPieDataset",  new JRBeanCollectionDataSource(getChartPieData(structureData)));
        chartParameters.put("chartBarHoursDataset",  new JRBeanCollectionDataSource(getChartBarData(structureData)));
        chartParameters.put("chartBarPeopleDataset",  new JRBeanCollectionDataSource(getChartBarData(structureData)));
        chartParameters.put("logo", new ByteArrayInputStream(logo));

        System.out.println("********************************************");
        System.out.println(getClass().getClassLoader().getResourceAsStream(filePath));
        JasperReport chartReport = JasperCompileManager.compileReport(getClass().getClassLoader().getResourceAsStream(filePathDependency));

        parameters.put("chartReport", chartReport);
        parameters.put("chartParameter", chartParameters);
        parameters.put("logo", new ByteArrayInputStream(logo));
        parameters.put("hoursPerMonth", HOURS_PER_MONTH);

        parameters.put("logo", new ByteArrayInputStream(getImageBytes()));
        parameters.put("entity", "Universidad Distrital Francisco José de Caldas".toUpperCase());

        parameters.put("levels", levels.stream().map(e -> e.getDescripcion().substring(0, 3).toUpperCase()).toList());

        return reportJR.converterToPDF(parameters, structureDataSource, filePath);
    }

    private List<ReportChartDTO> getChartPieData(List<ReportStructureDTO> structureData) {
        List<ReportChartDTO> list = new ArrayList<>();
        String structureName = null;
        
        ReportChartDTO  reportChartDTO = null;
        for (ReportStructureDTO e : structureData) {
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


    private List<ReportChartDTO> getChartBarData(List<ReportStructureDTO> structureData) {
        List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
        List<ReportChartDTO> list = new ArrayList<>();
        String structureName = null;
        List<ReportChartDTO>  group = null;
        for (ReportStructureDTO e : structureData) {
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

    private void buildStructureData(List<EstructuraEntity> structures, String[] tipologyStructures, int level, List<ReportStructureDTO>  results){
        for (EstructuraEntity structure : structures){
            tipologyStructures[level] = structure.getNombre();
            if (structure.getSubEstructuras() != null && !structure.getSubEstructuras().isEmpty()){
                buildStructureData(structure.getSubEstructuras(), tipologyStructures, level+1, results);
            }else{
                String levl = null;
                String levelNomenclature = null;
                Double frecuency = null;
                Double minTime = null;
                Double meanTime = null;
                Double maxTime = null;
                Double standarTime = null;
                List<Double> tiemposPorNivel = new ArrayList<>();

                if (structure.getActividad() != null){
                    List<NivelEntity> levels = (List<NivelEntity>)registry.get("levels");
                    levelNomenclature = structure.getActividad().getNivel().getDescripcion().substring(0, 3).toUpperCase();
                    levl = String.format("%s (%s)", structure.getActividad().getNivel().getDescripcion(), levelNomenclature) ;
                    frecuency = structure.getActividad().getFrecuencia();
                    minTime = (double) Math.round((structure.getActividad().getTiempoMinimo() / 60.0)*100)/100;
                    meanTime = (double) Math.round((structure.getActividad().getTiempoPromedio() / 60.0)*100)/100;
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
                .nivel(levl)
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
}
