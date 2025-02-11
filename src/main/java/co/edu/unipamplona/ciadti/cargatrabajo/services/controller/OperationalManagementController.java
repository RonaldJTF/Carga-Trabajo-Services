package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadGestionEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ActividadGestionService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.OperationalManagementReportExcelJXLS;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.OperationalManagementReportPDF;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/operational-management")
public class OperationalManagementController {
    private final GestionOperativaService gestionOperativaService;
    private final TipologiaService tipologiaService;
    private final ActividadGestionService actividadGestionService;
    private final ConfigurationMediator configurationMediator;
    private final OperationalManagementReportExcelJXLS organizationChartReportExcelJXLS;
    private final OperationalManagementReportPDF operationalManagementReportPDF;

    @Operation(
        summary = "Obtener o listar las gestiones operativas",
        description = "Obtiene o lista las gestiones operativas de acuerdo a ciertas variables o parámetros" +
                "Args: id: identificador de la gestión operativa. " +
                "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase GestionOperativaEntity). " +
                "Returns: Objeto o lista de objetos con información de las gestiones operativas. " +
                "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> getOperationalManagement(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(GestionOperativaEntity.class);
        GestionOperativaEntity filter = (GestionOperativaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, gestionOperativaService.findAllFilteredBy(filter));
    }

    @Operation(
        summary = "Crear una gestión operativa",
        description = "Crea una gestión operativa" +
                "Args: gestionOperativaEntity: objeto con información de la gestión operativa a registrar." +
                "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> createOperationalManagement(@Valid @RequestBody GestionOperativaEntity gestionOperativaEntity) throws CiadtiException {
        TipologiaEntity firstTypology = tipologiaService.findFirstTipology();
        TipologiaEntity secondTypology = tipologiaService.findById(firstTypology.getIdTipologiaSiguiente());
        gestionOperativaEntity.setIdTipologia(gestionOperativaEntity.getIdTipologia() == null ? secondTypology.getId() : gestionOperativaEntity.getIdTipologia());
        gestionOperativaEntity.setTipologia(tipologiaService.findById(gestionOperativaEntity.getIdTipologia()));

        Long lastOrder = gestionOperativaService.findLastOrderByIdPadre(gestionOperativaEntity.getIdPadre());
        gestionOperativaEntity.setOrden(
            gestionOperativaEntity.getOrden() != null
            ? gestionOperativaEntity.getOrden()
            : (lastOrder != null ? lastOrder + 1 : 1)
        );
        return new ResponseEntity<>(configurationMediator.createOperationalManagement(gestionOperativaEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Actualizar una gestión operativa",
        description = "Actualiza una gestión operativa. " +
                "Args: gestionOperativaEntity: objeto con información de la gestión operativa." +
                "id: identificador de la gestión operativa." +
                "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOperationalManagement(@Valid @RequestBody GestionOperativaEntity gestionOperativaEntity, @PathVariable Long id) throws CiadtiException {
        GestionOperativaEntity gestionOperativaEntityBD = gestionOperativaService.findById(id);
        Long previusOrder = gestionOperativaEntityBD.getOrden();
        Long lastOrder = gestionOperativaService.findLastOrderByIdPadre(gestionOperativaEntityBD.getIdPadre());
        gestionOperativaEntityBD.setOrden(
                gestionOperativaEntity.getOrden() != null
                ? gestionOperativaEntity.getOrden()
                : (lastOrder != null ? lastOrder + 1 : 1)
        );
        gestionOperativaEntityBD.setDescripcion(gestionOperativaEntity.getDescripcion());
        gestionOperativaEntityBD.setNombre(gestionOperativaEntity.getNombre());
        return new ResponseEntity<>(configurationMediator.updateOperationalManagement(gestionOperativaEntityBD, previusOrder), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar gestión operativa por el id",
        description = "Elimina una gestión operativa por su id." +
                "Args: id: identificador de la gestión operativa a eliminar.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOperationalManagement(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteOperationalManagement(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar gestiones operativas por el id",
            description = "Elimina lista de gestiones operativas por su id." +
                    "Args: operationManagementIds: identificadores de las gestiones operativas a eliminar.")
    @DeleteMapping
    public ResponseEntity<?> deleteOperationalsManagements(@RequestBody List<Long> operationManagementIds) throws CiadtiException {
        configurationMediator.deleteOperationalsManagements(operationManagementIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/migrate-structures")
    public ResponseEntity<?> migrateStructures(@RequestBody ArrayList<EstructuraEntity> estructuras, @RequestParam(required = false) Long idParent) throws CiadtiException, CloneNotSupportedException {
        return new ResponseEntity<>(configurationMediator.migrateStructures(estructuras, idParent), HttpStatus.CREATED);
    }

     @Operation(
            summary = "Obtener o listar las actividades",
            description = "Obtiene o lista las actividades de acuerdo a ciertas variables o parámetros. " +
                    "Args: id: identificador de la actividad. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase ActividadGestionEntity). " +
                    "Returns: Objeto o lista de objetos con información de la actividad. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"/activity", "/activity/{id}"})
    public ResponseEntity<?> getActivity(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(ActividadGestionEntity.class);
        ActividadGestionEntity filter = (ActividadGestionEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, actividadGestionService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear una actividad o detalle de la gestión operativa de tipología actividad",
            description = "Crea una actividad o detalle de la gestión operativa de tipología actividad. " +
                    "Args: actividadGestionEntity: objeto con información de la actividad. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("/activity")
    public ResponseEntity<?> createActivity(@Valid @RequestBody ActividadGestionEntity actividadGestionEntity) {
        return new ResponseEntity<>(actividadGestionService.save(actividadGestionEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una actividad o detalle de la gestión operativa de tipología actividad",
            description = "Actualiza una actividad o detalle de la gestión operativa de tipología actividad. " +
                    "Args: ActividadGestionEntity: objeto con información de la actividad. " +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/activity/{id}")
    public ResponseEntity<?> updateActivity(@Valid @RequestBody ActividadGestionEntity actividadGestionEntity, @PathVariable Long id) throws CiadtiException {
        ActividadGestionEntity actividadGestionEntityBD = actividadGestionService.findById(id);
        actividadGestionEntityBD.setFrecuencia(actividadGestionEntity.getFrecuencia());
        actividadGestionEntityBD.setTiempoMinimo(actividadGestionEntity.getTiempoMinimo());
        actividadGestionEntityBD.setTiempoMaximo(actividadGestionEntity.getTiempoMaximo());
        actividadGestionEntityBD.setTiempoPromedio(actividadGestionEntity.getTiempoPromedio());
        actividadGestionEntityBD.setIdGestionOperativa(actividadGestionEntity.getIdGestionOperativa());
        actividadGestionEntityBD.setIdNivel(actividadGestionEntity.getIdNivel());
        return new ResponseEntity<>(actividadGestionService.save(actividadGestionEntityBD), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar una actividad por su id",
            description = "Elimina una actividad por su id." +
                    "Args: id: identificador de la actividad a eliminar.")
    @DeleteMapping("/activity/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) throws CiadtiException {
        actividadGestionService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @SuppressWarnings("null")
    @GetMapping("report")
    public ResponseEntity<?> downloadReportExcel(@RequestParam(name = "type", required = false) String type,
                                                 @RequestParam(name = "operationalManagementIds", required = false) String operationalManagementIdsString) throws Exception {
        operationalManagementIdsString = operationalManagementIdsString.replaceAll("\\[|\\]|\\s", "");
        List<Long> operationalManagementIds = new ArrayList<>();
        if (!operationalManagementIdsString.isEmpty()) {
            String[] parts = operationalManagementIdsString.split(",");
            for (String part : parts) {
                operationalManagementIds.add(Long.parseLong(part));
            }
        }
        byte[] fileBytes = null;
        String extension = null;
        String mediaType = null;

        if ("excel".equalsIgnoreCase(type)) {
            extension = "xlsx";
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            fileBytes = organizationChartReportExcelJXLS.generate(operationalManagementIds);
        }else if ("pdf".equalsIgnoreCase(type)) {
            extension = "pdf";
            mediaType = "application/pdf";
            fileBytes = operationalManagementReportPDF.generate(operationalManagementIds);
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

    @PostMapping("/migrate-activities")
    public ResponseEntity<?> migrateActivity() throws Exception{
        configurationMediator.migrateActivity();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
