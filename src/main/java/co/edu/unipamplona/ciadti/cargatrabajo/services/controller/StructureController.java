package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.StructureReportPlainedExcelJXLS;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.StructureTimeStatisticsReportPDF;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ActividadService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.StructureReportExcelJXLS;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.StructureReportPDF;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/structure")
public class StructureController {
    private final EstructuraService estructuraService;
    private final TipologiaService tipologiaService;
    private final ActividadService actividadService;
    private final ConfigurationMediator configurationMediator;
    private final StructureReportPDF structureReportPDF;
    private final StructureTimeStatisticsReportPDF structureTimeStatisticsReportPDF;
    private final StructureReportExcelJXLS structureReportExcelJXLS;
    private final StructureReportPlainedExcelJXLS structureReportFlatExcelJXLS;

    @Operation(
            summary = "Obtener o listar las estructuras (Dependencia, Procesos, Procedimientos, Actividad, etc.)",
            description = "Obtiene o lista las estructuras de acuerdo a ciertas variables o parámetros. " +
                    "Args: id: identificador de la estructura. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase EstructuraEntity). " +
                    "Returns: Objeto o lista de objetos con información de la estructura. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(EstructuraEntity.class);
        EstructuraEntity filter = (EstructuraEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, estructuraService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Obtener las dependencias junto a sus subdependencias",
            description = "Obtiene las dependencias que existen junto a sus subdependencias."
    )
    @GetMapping({"/dependencies"})
    public ResponseEntity<?> getDependencies() throws CiadtiException {
        List<EstructuraEntity> dependencies = this.configurationMediator.getDependencies();
        return new ResponseEntity<>(dependencies, HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener la dependencia con la información de los procesos por su id (O estructuras de la tipologia que lo sigue) junto a sus subdependencias, pero si los procesos para esas subdependencias. ",
            description = "Obtiene la dependencia con la información de los procesos por el id (O estructuras de la tipologia que lo sigue) junto a sus subdependencias, pero si los procesos para esas subdependencias. "
    )
    @GetMapping({"/dependency/{idDependency}"})
    public ResponseEntity<?> getDependencyInformation(@PathVariable(required = true) Long idDependency) throws CiadtiException {
        return new ResponseEntity<>(this.configurationMediator.getDependencyInformation(idDependency), HttpStatus.OK);
    }

    @Operation(
            summary = "Crear una estructura junto a sus subestructuras",
            description = "Crea una estructura junto a sus subestructuras si estas son definidas. " +
                    "Args: estructuraEntity: objeto con información de la estructura. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestParam("structure") String estructuraJSON,
                                    @RequestParam(value = "file", required = false) MultipartFile file) throws IOException, CiadtiException {
        ObjectMapper objectMapper = new ObjectMapper();
        EstructuraEntity estructuraEntity = objectMapper.readValue(estructuraJSON, EstructuraEntity.class);
        TipologiaEntity firstTypology = tipologiaService.findFirstTipology();
        TipologiaEntity dependency = tipologiaService.findDependencyTipology();

        estructuraEntity.setIdTipologia(estructuraEntity.getIdTipologia() == null ? firstTypology.getId() : estructuraEntity.getIdTipologia());
        estructuraEntity.setTipologia(tipologiaService.findById(estructuraEntity.getIdTipologia()));

        if (estructuraEntity.getIdTipologia() != dependency.getId()) {
            Long lastOrder = estructuraService.findLastOrderByIdPadre(estructuraEntity.getIdPadre());
            estructuraEntity.setOrden(
                    estructuraEntity.getOrden() != null
                            ? estructuraEntity.getOrden()
                            : (lastOrder != null ? lastOrder + 1 : 1)
            );
        }

        if (file != null) {
            estructuraEntity.setMimetype(file.getContentType());
            estructuraEntity.setIcono(file.getBytes());
        }
        return new ResponseEntity<>(configurationMediator.createStructure(estructuraEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una estructura junto a sus subestructuras",
            description = "Actualiza una estructura junto a sus subestructuras si estas son definidas. " +
                    "Args: estructuraEntity: objeto con información de la estructura. " +
                    "id: identificador de la estructura. " +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestParam("structure") String estructuraJSON,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    @PathVariable Long id) throws CiadtiException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        EstructuraEntity estructuraEntity = objectMapper.readValue(estructuraJSON, EstructuraEntity.class);

        EstructuraEntity estructuraEntityBD = estructuraService.findById(id);
        Long previusOrder = estructuraEntityBD.getOrden();
        TipologiaEntity dependency = tipologiaService.findDependencyTipology();
        if (estructuraEntityBD.getIdTipologia() != dependency.getId()) {
            Long lastOrder = estructuraService.findLastOrderByIdPadre(estructuraEntityBD.getIdPadre());
            estructuraEntityBD.setOrden(
                    estructuraEntity.getOrden() != null
                            ? estructuraEntity.getOrden()
                            : (lastOrder != null ? lastOrder + 1 : 1)
            );
        }
        estructuraEntityBD.setNombre(estructuraEntity.getNombre());
        estructuraEntityBD.setDescripcion(estructuraEntity.getDescripcion());

        if (file != null) {
            estructuraEntityBD.setMimetype(file.getContentType());
            estructuraEntityBD.setIcono(file.getBytes());
        }
        return new ResponseEntity<>(configurationMediator.updateStructure(estructuraEntityBD, previusOrder), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar una estructura por su id",
            description = "Elimina una estructura y sus subestructuras por su id." +
                    "Args: id: identificador de la estructura a eliminar.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteStructure(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteStructures(@RequestBody List<Long> structureIds) throws CiadtiException {
        configurationMediator.deleteStructures(structureIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Obtener o listar las actividades",
            description = "Obtiene o lista las actividades de acuerdo a ciertas variables o parámetros. " +
                    "Args: id: identificador de la actividad. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase ActividadEntity). " +
                    "Returns: Objeto o lista de objetos con información de la actividad. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"/activity", "/activity/{id}"})
    public ResponseEntity<?> getActivity(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(ActividadEntity.class);
        ActividadEntity filter = (ActividadEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, actividadService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear una actividad o detalle de la estructura de tipología actividad",
            description = "Crea una actividad o detalle de la estructura de tipología actividad. " +
                    "Args: actividadEntity: objeto con información de la actividad. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/activity")
    public ResponseEntity<?> createActivity(@Valid @RequestBody ActividadEntity actividadEntity) {
        return new ResponseEntity<>(actividadService.save(actividadEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una actividad o detalle de la estructura de tipología actividad",
            description = "Actualiza una actividad o detalle de la estructura de tipología actividad. " +
                    "Args: actividadEntity: objeto con información de la actividad. " +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/activity/{id}")
    public ResponseEntity<?> updateActivity(@Valid @RequestBody ActividadEntity actividadEntity, @PathVariable Long id) throws CiadtiException {
        ActividadEntity actividadEntityBD = actividadService.findById(id);
        actividadEntityBD.setFrecuencia(actividadEntity.getFrecuencia());
        actividadEntityBD.setTiempoMinimo(actividadEntity.getTiempoMinimo());
        actividadEntityBD.setTiempoMaximo(actividadEntity.getTiempoMaximo());
        actividadEntityBD.setTiempoPromedio(actividadEntity.getTiempoPromedio());
        actividadEntityBD.setIdEstructura(actividadEntity.getIdEstructura());
        actividadEntityBD.setIdNivel(actividadEntity.getIdNivel());
        return new ResponseEntity<>(actividadService.save(actividadEntityBD), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar una actividad por su id",
            description = "Elimina una actividad por su id." +
                    "Args: id: identificador de la actividad a eliminar.")
    @DeleteMapping("/activity/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) throws CiadtiException {
        actividadService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @SuppressWarnings("null")
    @GetMapping("/report")
    public ResponseEntity<?> downloadReportExcel(@RequestParam(name = "type", required = false) String type,
                                                 @RequestParam(name = "structureIds", required = false) String structureIdsString) throws Exception {
        structureIdsString = structureIdsString.replaceAll("\\[|\\]|\\s", "");
        List<Long> structureIds = new ArrayList<>();
        if (!structureIdsString.isEmpty()) {
            String[] parts = structureIdsString.split(",");
            for (String part : parts) {
                structureIds.add(Long.parseLong(part));
            }
        }
        byte[] fileBytes = null;
        String extension = null;
        String mediaType = null;

        if (type == null || "EXCEL".equals(type.toUpperCase())) {
            extension = "xlsx";
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            fileBytes = structureReportExcelJXLS.generate(structureIds);
        }else if (type == null || "FLAT-EXCEL".equals(type.toUpperCase())) {
            extension = "xlsx";
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            fileBytes = structureReportFlatExcelJXLS.generateExcel(structureIds);
        }else if ("PDF".equals(type.toUpperCase())) {
            extension = "pdf";
            mediaType = "application/pdf";
            fileBytes = structureReportPDF.generate(structureIds);
        }else if ("TIME-STATISTICS-PDF".equals(type.toUpperCase())) {
            extension = "pdf";
            mediaType = "application/pdf";
            /*Para reportes de estadística de tiempos solo se requiere de una estructura, por eso se toma el único que viene*/
            fileBytes = structureTimeStatisticsReportPDF.generate(structureIds.get(0));
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
            summary = "Mover una estructura a un nuevo padre",
            description = "Mueve una estructura a un nuevo padre." +
                    "Args: newParentId: identificador de la nueva estructura padre. " + 
                    "structureId: Identificador de la estructura a ser movida. " + 
                    "Returns: Objeto movido con nueva información asociada.")
    @PutMapping("/move/{newParentId}")
    public ResponseEntity<?> updateMovedStructure(@PathVariable("newParentId") Long newParentId, @RequestParam("movedStructureId") Long structureId) throws CiadtiException {
        EstructuraEntity movedStructure = estructuraService.findById(structureId);
        configurationMediator.moveStructure(movedStructure, newParentId);
        return new ResponseEntity<>(movedStructure, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Copiar una estructura a otra",
            description = "Copia una estructura en otra estructura." +
                    "Args: newParentId: identificador de la estructura padre donde será copiada. " + 
                    "structureId: Identificador de la estructura a ser copiada. " + 
                    "Returns: Objeto copiado con nueva información asociada al padre.")
    @PostMapping("/copy/{newParentId}")
    public ResponseEntity<?> pasteStructure(@PathVariable Long newParentId, @RequestParam("copiedStructureId") Long structureId) throws Exception {
        EstructuraEntity structureToCopy = estructuraService.findById(structureId);
        EstructuraEntity copiedStructure = (EstructuraEntity) structureToCopy.clone();
        Long order = estructuraService.findLastOrderByIdPadre(newParentId);
        copiedStructure.setOrden(order + 1);
        configurationMediator.copyStructure(copiedStructure, newParentId);
        return new ResponseEntity<>(copiedStructure, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Reasignar una estructura en otra",
        description = "Reasigna una estructura en otra estructura." +
                "Args: newParentId: identificador de la estructura padre donde será reasignada. " + 
                "structureId: Identificador de la estructura a ser reasignada. " + 
                "Returns: Objeto reasignado con nueva información asociada al padre. " +
                "Nota: Al ser reasignada, la tipología de la estructura pasa a ser de la tipología siguiente del nuevo padre.")
    @PutMapping("/reasign/{newParentId}")
    public ResponseEntity<?> updateReasignatedStructure(@PathVariable("newParentId") Long newParentId, @RequestParam("reassignedStructureId") Long structureId) throws Exception {
        EstructuraEntity movedStructure = estructuraService.findById(structureId);
        Long order = estructuraService.findLastOrderByIdPadre(newParentId);
        movedStructure.setOrden(order + 1);
        configurationMediator.reasignStructure(movedStructure, newParentId);
        return new ResponseEntity<>(movedStructure, HttpStatus.CREATED);
    }
}
