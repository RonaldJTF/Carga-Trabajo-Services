package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ReportAppointmentDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ReportAppointmentDTO.ComparativeAttribute;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.AlcanceEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CategoriaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLaboralEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.StaticResourceMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.MultiPropertyComparator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.comparator.PropertyComparator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.EachMergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.HeaderCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.ImageCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.MergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.StyleCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.functions.ReportFunction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AppointmentReportExcelJXLS {
    private final ConfigurationMediator configurationMediator;
    private final StaticResourceMediator staticResourceMediator;
    private Map<String, Object> registry;

    public byte[] generate(Map<String, Long[]> filters) throws Exception{
        registry = new HashMap<>();

        List<CargoEntity> appointments = configurationMediator.findAppointments(filters);  

        generateDataset(appointments);
        generateComparativeDataset(appointments);

        String filePath = "reports/appointments/Appointments.xlsx";
        Map<String, Object> contextMap = new HashMap<String, Object>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String reportDate = dateFormat.format(new Date());

        ReportFunction r = new ReportFunction();
        contextMap.put("R", r);

        contextMap.put("logo", this.staticResourceMediator.getResourceBytes("reports/images/logo.png"));
        contextMap.put("reportDate", reportDate);
        
        contextMap.put("dataset", registry.get("dataset"));
        contextMap.put("categories", registry.get("categories"));
        contextMap.put("totalCompensations", registry.get("totalCompensations"));
        Resource resource = this.staticResourceMediator.getResource(filePath);
        contextMap.put("comparativeDataset", registry.get("comparativeDataset"));
        contextMap.put("comparativesByScope", registry.get("comparativesByScope"));

        byte[] bytes = JxlsPoiTemplateFillerBuilder
            .newInstance()
            .withTemplate(resource.getInputStream())
            .withCommand("merge", MergeCommand.class)
            .withCommand("image", ImageCommand.class)
            .withCommand("style", StyleCommand.class)
            .withCommand("eachMerge", EachMergeCommand.class)
            .withCommand("header", HeaderCommand.class)
            .needsPublicContext(r)
            .buildAndFill(contextMap);
        return bytes;
    }

    @SuppressWarnings("null")
    private void generateDataset(List<CargoEntity> appointments) throws Exception{
        List<CategoriaEntity> categories = this.getCategories(appointments);
        List<ReportAppointmentDTO> dataset = new ArrayList<>();

        appointments.sort(Comparator
            .comparing((CargoEntity appointment) -> appointment.getJerarquia().getIdOrganigrama())
            .thenComparing(CargoEntity::getIdAlcance)
            .thenComparing(CargoEntity::getIdVigencia)
            .thenComparing((CargoEntity appointment) -> appointment.getJerarquia().getIdDependencia())
            .thenComparing(CargoEntity::getIdNormatividad)
            .thenComparing(CargoEntity::getIdNivel)
            .thenComparing(CargoEntity::getIdEscalaSalarial));

        Long idOrganigrama = -1L;
        Long idAlcance = -1L;
        Long idVigencia = -1L;
        Long idDependencia = -1L;
        Long idNormatividad = -1L;
        Long idNivel = -1L;

        ReportAppointmentDTO org = null;
        ReportAppointmentDTO scope = null;
        ReportAppointmentDTO validity = null;
        ReportAppointmentDTO dependency = null;
        ReportAppointmentDTO dependencyAppointment = null;
        ReportAppointmentDTO normativity = null;
        ReportAppointmentDTO level = null;

        for (CargoEntity e : appointments){
            if (!idOrganigrama.equals(e.getJerarquia().getIdOrganigrama())){
                org = ReportAppointmentDTO.builder().data(e.getJerarquia().getOrganigrama()).children(new ArrayList<>()).build();
                dataset.add(org);
                idOrganigrama = e.getJerarquia().getIdOrganigrama();
                idAlcance = -1L;
            }
            if (!idAlcance.equals(e.getIdAlcance())){
                scope = ReportAppointmentDTO.builder().data(e.getAlcance()).children(new ArrayList<>()).build();
                org.getChildren().add(scope);
                idAlcance = e.getIdAlcance();
                idVigencia = -1L;
            }
            if(!idVigencia.equals(e.getIdVigencia())){
                validity = ReportAppointmentDTO.builder().data(e.getVigencia()).children(new ArrayList<>()).build();
                scope.getChildren().add(validity);
                idVigencia = e.getIdVigencia();
                idDependencia = -1L;
            }
            if(!idDependencia.equals(e.getJerarquia().getIdDependencia())){
                dependency = ReportAppointmentDTO.builder().data(e.getJerarquia().getDependencia()).children(new ArrayList<>()).build();
                validity.getChildren().add(dependency);

                dependencyAppointment = ReportAppointmentDTO.builder().children(new ArrayList<>()).totalCargos(0).build();
                dependency.getChildren().add(dependencyAppointment);

                idDependencia = e.getJerarquia().getIdDependencia();
                idNormatividad = -1L;
            }
            if(!idNormatividad.equals(e.getIdNormatividad())){
                normativity = ReportAppointmentDTO.builder().data(e.getNormatividad()).children(new ArrayList<>()).build();
                dependencyAppointment.getChildren().add(normativity);
                idNormatividad = e.getIdNormatividad();
                idNivel = -1L;
            }
            if(!idNivel.equals(e.getIdNivel())){
                level = ReportAppointmentDTO.builder().data(e.getNivel()).children(new ArrayList<>()).build();
                normativity.getChildren().add(level);
                idNivel = e.getIdNivel();
            }

            level.getChildren().add(
                ReportAppointmentDTO
                    .builder()
                    .data(e.getEscalaSalarial())
                    .totalCargos(e.getTotalCargos())
                    .asignacionBasicaMensual(e.getAsignacionBasicaMensual())
                    .valueByCompensation(getValueByCompensation(e))
                    .build());
            dependencyAppointment.setTotalCargos(dependencyAppointment.getTotalCargos() + e.getTotalCargos());
        }
        registry.put("dataset", dataset);
        registry.put("categories", categories);
        registry.put("totalCompensations", getTotalCompensations());
    } 

    @SuppressWarnings("null")
    private void generateComparativeDataset(List<CargoEntity> appointments) throws Exception{
        List<ReportAppointmentDTO> dataset = new ArrayList<>();
        List<ReportAppointmentDTO.ComparativeAttribute> comparativesByScope = buildComperatives(appointments);

        appointments.sort(Comparator
            .comparing((CargoEntity appointment) -> appointment.getJerarquia().getIdOrganigrama())
            .thenComparing((CargoEntity appointment) -> appointment.getJerarquia().getIdDependencia())
            .thenComparing(CargoEntity::getIdNivel));

        Long idOrganigrama = -1L;
        Long idDependencia = -1L;
        Long idNivel = -1L;
        ReportAppointmentDTO organizationChart = null;
        ReportAppointmentDTO dependency = null;
        ReportAppointmentDTO level = null;
        
        for (CargoEntity e : appointments){
            if(!idOrganigrama.equals(e.getJerarquia().getIdOrganigrama())){
                organizationChart = ReportAppointmentDTO.builder().data(e.getJerarquia().getOrganigrama()).children(new ArrayList<>()).build();
                dataset.add(organizationChart);
                idOrganigrama = e.getJerarquia().getIdOrganigrama();
                idDependencia = -1L;
            }
            if(!idDependencia.equals(e.getJerarquia().getIdDependencia())){
                dependency = ReportAppointmentDTO.builder().data(e.getJerarquia().getDependencia()).children(new ArrayList<>()).build();
                organizationChart.getChildren().add(dependency);
                idDependencia = e.getJerarquia().getIdDependencia();
                idNivel = -1L;
            }
            if(!idNivel.equals(e.getIdNivel())){
                level = ReportAppointmentDTO
                    .builder()
                    .data(e.getNivel())
                    .comparativesByScope(new ArrayList<>())
                    .children(new ArrayList<>())
                    .build();
                dependency.getChildren().add(level);
                idNivel = e.getIdNivel();
            }

            Double totalAnualPorCargo = e.getCompensacionesLaboralesAplicadas().stream().mapToDouble(clnv -> clnv.getValorAplicado() != null ? clnv.getValorAplicado() : 0.0).sum();
            totalAnualPorCargo += e.getAsignacionBasicaAnual();

            for (ComparativeAttribute obj : comparativesByScope){
                ComparativeAttribute attr = getComparativeAttribute(level.getComparativesByScope(), obj.getKey());
                if(attr == null){
                    attr = ComparativeAttribute.builder().asignacionBasicaAnual(0.0).key(obj.getKey()).totalCargos(0).build();
                    level.getComparativesByScope().add(attr);
                }
                if(e.getIdAlcance().toString().equals(obj.getKey())){
                    attr.setAsignacionBasicaAnual(attr.getAsignacionBasicaAnual() + totalAnualPorCargo*e.getTotalCargos());
                    attr.setTotalCargos(attr.getTotalCargos() + e.getTotalCargos());
                }
            }
        }

        for(ReportAppointmentDTO ra : dataset){
           for(ReportAppointmentDTO s : ra.getChildren()){
                for (ReportAppointmentDTO l : s.getChildren()){
                    for(ComparativeAttribute ca : l.getComparativesByScope()){
                        if(ca.getKey().contains("-")){
                            String[] partes = ca.getKey().split("-");
                            String key1 = partes[0];
                            String key2 = partes[1]; 
                            ComparativeAttribute attr1 = getComparativeAttribute(l.getComparativesByScope(), key1);
                            ComparativeAttribute attr2 = getComparativeAttribute(l.getComparativesByScope(), key2);
                            ca.setAsignacionBasicaAnual(attr1.getAsignacionBasicaAnual() - attr2.getAsignacionBasicaAnual());
                            ca.setTotalCargos(attr1.getTotalCargos() - attr2.getTotalCargos());
                        }
                    }
                }
           }
        }
        registry.put("comparativeDataset", dataset);
        registry.put("comparativesByScope", comparativesByScope);
    }

    private ComparativeAttribute getComparativeAttribute(List<ComparativeAttribute> comparativesByScope, String key){
        for(ComparativeAttribute ca : comparativesByScope){
            if(ca.getKey().equals(key)){
                return ca;
            }
        }
        return null;
    }

    private Integer getTotalCompensations(){
        @SuppressWarnings("unchecked")
        List<CategoriaEntity> categories = (List<CategoriaEntity>) registry.get("categories");
        return categories.stream().mapToInt(item -> item.getCompensaciones().size()).sum();
    }

    private Map<Long, Double> getValueByCompensation(CargoEntity appointment){
        Map<Long, Double> valueByCompensation = new HashMap<>();
        for (CompensacionLabNivelVigenciaEntity clnv : appointment.getCompensacionesLaboralesAplicadas()){
            valueByCompensation.put(clnv.getIdCompensacionLaboral(), clnv.getValorAplicado());
        }
        return valueByCompensation;
    }

    private List<CategoriaEntity> getCategories(List<CargoEntity> appointments){
        List<CategoriaEntity> categories = new ArrayList<>();
        if (appointments == null){
            return categories;
        }
        Map<Long, Map<Long, CompensacionLaboralEntity>> all = new HashMap<>();
        for(CargoEntity appointment : appointments){
            for (CompensacionLabNivelVigenciaEntity clnv : appointment.getCompensacionesLaboralesAplicadas()){
                Long categoryId = clnv.getCompensacionLaboral().getIdCategoria();
                Long compensationId = clnv.getIdCompensacionLaboral();
                if(!all.containsKey(categoryId)){
                    all.put(categoryId, new HashMap<>());
                }

                if(!all.get(categoryId).containsKey(compensationId)){
                    all.get(categoryId).put(compensationId, clnv.getCompensacionLaboral());
                }
            }
        }
        for (Map.Entry<Long, Map<Long, CompensacionLaboralEntity>> categoryEntry : all.entrySet()) {
            Long categoryKey = categoryEntry.getKey();
            Map<Long, CompensacionLaboralEntity> compensationMap = categoryEntry.getValue();
            List<CompensacionLaboralEntity> compensations = new ArrayList<>(compensationMap.values());
            String categoryName = compensations.get(0).getCategoria().getNombre();
            CategoriaEntity category = CategoriaEntity
                .builder()
                .id(categoryKey)
                .nombre(categoryName)
                .compensaciones(compensations)
                .build();
            categories.add(category);
        }
        return categories;
    }

    private List<AlcanceEntity> getScopes(List<CargoEntity> appointments){
        List<AlcanceEntity> scopes = new ArrayList<>();
        if (appointments == null){
            return scopes;
        }
        Map<Long, AlcanceEntity> all = new HashMap<>();
        for(CargoEntity appointment : appointments){
           if(!all.containsKey(appointment.getIdAlcance())){
                all.put(appointment.getIdAlcance(), appointment.getAlcance());
           }
        }
        scopes = new ArrayList<>(all.values());
        return scopes;
    }

    private List<ReportAppointmentDTO.ComparativeAttribute> buildComperatives(List<CargoEntity> appointments){
        List<AlcanceEntity> scopes = getScopes(appointments);
        List<ReportAppointmentDTO.ComparativeAttribute> comparativesByScope = new ArrayList<>();
        ComparativeAttribute attr;

        for (AlcanceEntity scope : scopes){
            attr = ReportAppointmentDTO.ComparativeAttribute.builder().key(scope.getId().toString()).name(scope.getNombre()).build();
            comparativesByScope.add(attr);
        }

        for (int i=0; i < scopes.size(); i++){
            AlcanceEntity scope_i = scopes.get(i);
            for (int j=i+1; j < scopes.size(); j++){
                AlcanceEntity scope_j = scopes.get(j);
                attr = ReportAppointmentDTO.ComparativeAttribute.builder().key(scope_i.getId() + "-" + scope_j.getId()).name("DIFERENCIAS ENTRE " + scope_i.getNombre() + " Y " + scope_j.getNombre()).build();
                comparativesByScope.add(attr);
            }
        }
        return comparativesByScope;
    }
}