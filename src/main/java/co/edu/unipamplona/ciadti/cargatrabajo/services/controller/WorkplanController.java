package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.WorkplanReportExcel;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/workplan")
public class WorkplanController {
    private final PlanTrabajoService planTrabajoService;
    private final EtapaService etapaService;
    private final TareaService tareaService;
    private final SeguimientoService seguimientoService;
    private final ConfigurationMediator configurationMediator;
    private final WorkplanReportExcel workplanReportExcel;

    @Operation(
        summary = "Obtener o listar los planes de trabajo",
        description = "Obtiene o lista los planes de trabajo de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador del plan de trabajo. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase PlanTrabajoEntity). " +
            "Returns: Objeto o lista de objetos con información del plan de trabajo. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(PlanTrabajoEntity.class);
        PlanTrabajoEntity filter = (PlanTrabajoEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        List<PlanTrabajoEntity> result = planTrabajoService.findAllFilteredBy(filter);
        //Map<Long, Double> advances = planTrabajoService.getAllAvances();
        result.forEach(planTrabajo -> {
            //Double avance = advances.get(planTrabajo.getId());
            List<EtapaEntity> stages = configurationMediator.findAllStagesByIdWorkplan(planTrabajo.getId());
            Double avance = 0.0;
            Integer totalTareas = 0;
            for (EtapaEntity e : stages){
                avance += (e.getAvance() != null ? e.getAvance() : 0) / stages.size();
                totalTareas += (e.getTotalTareas() != null ? e.getTotalTareas() : 0);
            }
            planTrabajo.setAvance(avance);
            planTrabajo.setTotalEtapas(stages.size());
            planTrabajo.setTotalTareas(totalTareas);
        });
        return Methods.getResponseAccordingToId(id, result);
    }

   
    @Operation(
        summary="Crear un plan de trabajo",
        description = "Crea un plan de trabajo. " + 
            "Args: planTrabajoEntity: objeto con información del plan de trabajo. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> create (@Valid @RequestBody PlanTrabajoEntity planTrabajoEntity){
        return new ResponseEntity<>(planTrabajoService.save(planTrabajoEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar un plan de trabajo",
        description = "Actualiza un plan de trabajo. " + 
            "Args: planTrabajoEntity: objeto con información del plan de trabajo. " +
            "id: identificador del plan de trabajo. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update (@Valid @RequestBody PlanTrabajoEntity planTrabajoEntity, @PathVariable Long id) throws CiadtiException{
        PlanTrabajoEntity planTrabajoEntityBD = planTrabajoService.findById(id);
        planTrabajoEntityBD.setDescripcion(planTrabajoEntity.getDescripcion());
        planTrabajoEntityBD.setNombre(planTrabajoEntity.getNombre());
        return new ResponseEntity<>(planTrabajoService.save(planTrabajoEntityBD), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar plan de trabajo junto a sus etapas",
        description = "Elimina un plan de trabajo junto a sus etapas " + 
            "Args: id: identificador del plan de trabajo a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete (@PathVariable Long id){
        configurationMediator.deleteWorkplan(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteWorkplans(@RequestBody List<Long> workplanIds) throws CiadtiException {
        configurationMediator.deleteWorkplans(workplanIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener o listar las etapas de un plan de trabajo",
        description = "Obtiene o lista las etapas de un plan de trabajo de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador de la etapa. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase EtapaEntity). " +
            "Returns: Objeto o lista de objetos con información de la etapa en un plan de trabajo. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"/stage", "/stage/{id}"})
    public ResponseEntity<?> getStage(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(EtapaEntity.class);
        EtapaEntity filter = (EtapaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, etapaService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear una etapa para un plan de trabajo",
        description = "Crea una etapa para un plan de trabajo. " + 
            "Args: etapaEntity: objeto con información de la etapa. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping("/stage")
    public ResponseEntity<?> createStage (@Valid @RequestBody EtapaEntity etapaEntity){
        return new ResponseEntity<>(etapaService.save(etapaEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar una etapa",
        description = "Actualiza una etapa. " + 
            "Args: etapaEntity: objeto con información de la etapa. " +
            "id: identificador de la etapa. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/stage/{id}")
    public ResponseEntity<?> updateStage (@Valid @RequestBody EtapaEntity etapaEntity, @PathVariable Long id) throws CiadtiException{
        EtapaEntity etapaEntityBD = etapaService.findById(id);
        etapaEntityBD.setDescripcion(etapaEntity.getDescripcion());
        etapaEntityBD.setNombre(etapaEntity.getNombre());
        return new ResponseEntity<>(etapaService.save(etapaEntityBD), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar etapa del plan de trabajo junto a sus actividades",
        description = "Elimina una etapa de un plan de trabajo junto a sus actividades" + 
            "Args: id: identificador de la etapa a eliminar. ")
    @DeleteMapping("/stage/{id}")
    public ResponseEntity<?> deleteStage (@PathVariable Long id){
        configurationMediator.deleteStage(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/stage")
    public ResponseEntity<?> deleteStages(@RequestBody List<Long> stageIds) throws CiadtiException {
        configurationMediator.deleteStages(stageIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener o listar las tareas de una etapa",
        description = "Obtiene o lista las tareas de una etapa de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador de la tarea. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase TareaEntity). " +
            "Returns: Objeto o lista de objetos con información de la tarea en una etapa. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"/task", "/task/{id}"})
    public ResponseEntity<?> getTask(@PathVariable Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(TareaEntity.class);
        TareaEntity filter = (TareaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, tareaService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear una tarea para una etapa",
        description = "Crea una tarea para una etapa. " + 
            "Args: tareaEntity: objeto con información de la tarea. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping("/task")
    public ResponseEntity<?> createTask (@Valid @RequestBody TareaEntity tareaEntity){
        return new ResponseEntity<>(tareaService.save(tareaEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar una tarea",
        description = "Actualiza una tarea. " + 
            "Args: tareaEntity: objeto con información de la tarea. " +
            "id: identificador de la tarea. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/task/{id}")
    public ResponseEntity<?> updateStage (@Valid @RequestBody TareaEntity tareaEntity, @PathVariable Long id) throws CiadtiException{
        TareaEntity tareaEntityBD = tareaService.findById(id);
        tareaEntityBD.setDescripcion(tareaEntity.getDescripcion());
        tareaEntityBD.setNombre(tareaEntity.getNombre());
        tareaEntityBD.setEntregable(tareaEntity.getEntregable());
        tareaEntityBD.setResponsable(tareaEntity.getResponsable());
        tareaEntityBD.setFechaInicio(tareaEntity.getFechaInicio());
        tareaEntityBD.setFechaFin(tareaEntity.getFechaFin());
        return new ResponseEntity<>(tareaService.save(tareaEntityBD), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar fecha de realización de la tarea",
        description = "Actualiza fecha de realización de la tarea. " + 
            "Args: tareaEntity: objeto con información de las fechas de la tarea. " +
            "id: identificador de la tarea. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/task/dates/{id}")
    public ResponseEntity<?> updateStageDates (@Valid @RequestBody TareaEntity tareaEntity, @PathVariable Long id) throws CiadtiException{
        TareaEntity tareaEntityBD = tareaService.findById(id);
        tareaEntityBD.setFechaInicio(tareaEntity.getFechaInicio());
        tareaEntityBD.setFechaFin(tareaEntity.getFechaFin());
        return new ResponseEntity<>(tareaService.save(tareaEntityBD), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar una tarea junto a su seguimientos realizados",
        description = "Elimina una tarea junto a sus seguimientos" + 
            "Args: id: identificador de la tarea a eliminar. ")
    @DeleteMapping("/task/{id}")
    public ResponseEntity<?> deleteTask (@PathVariable Long id){
        configurationMediator.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/task")
    public ResponseEntity<?> deleteTasks(@RequestBody List<Long> taskIds) throws CiadtiException {
        configurationMediator.deleteTasks(taskIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener o listar los seguimientos de una tarea",
        description = "Obtiene o lista los seguimientos de una tarea de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador del seguimiento. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase SeguimientoEntity). " +
            "Returns: Objeto o lista de objetos con información del seguimiento en una tarea. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"/follow-up", "/follow-up/{id}"})
    public ResponseEntity<?> getFollowUp(@PathVariable Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(SeguimientoEntity.class);
        SeguimientoEntity filter = (SeguimientoEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, seguimientoService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear un seguimiento para una tarea",
        description = "Crea una seguimiento para una tarea. " + 
            "Args: seguimientoEntity: objeto con información del seguimiento. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping("/follow-up")
    public ResponseEntity<?> createFollowUp (@Valid @RequestParam(value = "followUp") String followUpJSON,
                                         @RequestParam(value = "files", required = false) List<MultipartFile> files) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        SeguimientoEntity seguimientoEntity = objectMapper.readValue(followUpJSON, SeguimientoEntity.class);
        return new ResponseEntity<>(configurationMediator.saveFollowUp(seguimientoEntity, files), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar un seguimiento",
        description = "Actualiza un seguimiento. " + 
            "Args: seguimientoEntity: objeto con información del seguimiento. " +
            "id: identificador del seguimiento. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/follow-up/{id}")
    public ResponseEntity<?> updateFollowUp(@Valid @RequestParam(value = "followUp") String followUpJSON,
                                            @RequestParam(value = "files", required = false) List<MultipartFile> files, 
                                            @PathVariable Long id) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        SeguimientoEntity seguimientoEntity = objectMapper.readValue(followUpJSON, SeguimientoEntity.class);
        SeguimientoEntity seguimientoEntityBD = seguimientoService.findById(id);
        seguimientoEntityBD.setPorcentajeAvance(seguimientoEntity.getPorcentajeAvance());
        seguimientoEntityBD.setObservacion(seguimientoEntity.getObservacion());
        return new ResponseEntity<>(configurationMediator.saveFollowUp(seguimientoEntityBD, files), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar un seguimiento junto a sus archivos soportes",
        description = "Elimina un seguimiento junto a sus archivos soportes. " + 
            "Args: id: identificador del seguimiento a eliminar. ")
    @DeleteMapping("/follow-up/{id}")
    public ResponseEntity<?> deleteFollowUp (@PathVariable Long id){
        configurationMediator.deleteFollowUp(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/report")
    public ResponseEntity<?> downloadReportExcel(@RequestParam(name = "type", required = false) String type,
                                                 @RequestParam(name = "stageIds", required = false) String stageIdsString,
                                                 @RequestParam(name = "idWorkplan", required = false) Long idWorkplan) throws CiadtiException{         
        List<Long> stageIds = null;
        if(stageIdsString != null){
            stageIdsString = stageIdsString.replaceAll("\\[|\\]|\\s", "");
            if (!stageIdsString.isEmpty()) {
                stageIds = new ArrayList<>();
                String[] parts = stageIdsString.split(",");
                for (String part : parts) {
                    stageIds.add(Long.parseLong(part));
                }
            }     
        }
           
        byte[] fileBytes = null;
        String extension = null;
        String mediaType = null;

        if(type == null || "EXCEL".equals(type.toUpperCase())){
            extension = "xlsx";
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            fileBytes = workplanReportExcel.generate(stageIds, idWorkplan);
        }

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String fileName = String.format("reporte_%s.%s", currentDateTime, extension);

        if (fileBytes != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition, Content-Type");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(mediaType))
                    .body(fileBytes);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }    


    @Operation(
        summary = "Obtener la información del avance de un plan de trabajo en función del tiempo.",
        description = "Obtiene la información del avance de un plan de trabajo en función del tiempo (por días, semana o mes). " + 
            "Args: id: identificador del plan de trabajo. " + 
            "type: tipo de tiempo por el que se realiza el consolidado (day, month, week)")
    @GetMapping("/consolidated/{id}")
    private ResponseEntity<?> getConsolidatedByTime(@PathVariable("id") Long id, @RequestParam(name = "timeType", required = false) String timeType) throws CiadtiException{
        return new ResponseEntity<>(configurationMediator.getConsolidatedByTime(id, timeType), HttpStatus.OK);
    }
}
