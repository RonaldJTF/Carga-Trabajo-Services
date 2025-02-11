package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ConvencionEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DependenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ConvencionService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.DependenciaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.JerarquiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.AssignedOperationalManagementReportExcelJXLS;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.AssignedOperationalManagementReportPlainedExcelJXLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.OrganigramaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.OrganigramaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization-chart")
public class OrganizationStructureController {
    private final OrganigramaService organigramaService;
    private final JerarquiaService jerarquiaService;
    private final DependenciaService dependenciaService;
    private final ConvencionService convencionService;
    private final ConfigurationMediator configurationMediator;
    private final AssignedOperationalManagementReportPlainedExcelJXLS organizationChartReportPlainedExcelJXLS;
    private final AssignedOperationalManagementReportExcelJXLS organizationChartReportExcelJXLS;
    private final GestionOperativaService gestionOperativaService;

    @Operation(
            summary = "Obtener o listar los organigramas",
            description = "Obtiene o lista los organigramas de acuerdo a ciertas variables o parámetros" +
                    "Args: id: identificador del organigrama. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase OrganigramaEntity). " +
                    "Returns: Objeto o lista de objetos con información de los organigramas. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> getOrganizationChart(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(OrganigramaEntity.class);
        OrganigramaEntity filter = (OrganigramaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, organigramaService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear un organigrama",
            description = "Crea un organigrama. " +
                    "Args: organizationChartJSON: objeto con información del organigrama a registrar." +
                    "file: imagen del diagrama del organigrama" +
                    "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> createOrganizationChart(@Valid @RequestParam("organizationChart") String organizationChartJSON,
                                                     @RequestParam(value = "file", required = false) MultipartFile file) throws IOException, CiadtiException {
        ObjectMapper objectMapper = new ObjectMapper();
        OrganigramaEntity organigramaEntity = objectMapper.readValue(organizationChartJSON, OrganigramaEntity.class);
        if (file != null) {
            organigramaEntity.setMimetype(file.getContentType());
            organigramaEntity.setDiagrama(file.getBytes());
        }
        return new ResponseEntity<>(organigramaService.save(organigramaEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un organigrama",
            description = "Actualiza un organigrama. " +
                    "Args: organizationChartJSON: objeto con información del organigrama." +
                    "file: imagen del diagrama del organigrama" +
                    "id: identificador del organigrama." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrganizationChart(@Valid @RequestParam("organizationChart") String organizationChartJSON,
                                                     @RequestParam(value = "file", required = false) MultipartFile file,
                                                     @PathVariable Long id) throws IOException, CiadtiException{
        ObjectMapper objectMapper = new ObjectMapper();
        OrganigramaEntity organigramaEntity = objectMapper.readValue(organizationChartJSON, OrganigramaEntity.class);

        OrganigramaEntity organigramaEntityBD = organigramaService.findById(id);
        organigramaEntityBD.setDescripcion(organigramaEntity.getDescripcion());
        organigramaEntityBD.setNombre(organigramaEntity.getNombre());
        organigramaEntityBD.setIdNormatividad(organigramaEntity.getIdNormatividad());
        if (file != null) {
            organigramaEntityBD.setMimetype(file.getContentType());
            organigramaEntityBD.setDiagrama(file.getBytes());
        }
        return new ResponseEntity<>(organigramaService.save(organigramaEntityBD), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar organigrama por el id",
            description = "Elimina un organigrama por su id." +
                    "Args: id: identificador del organigrama a eliminar.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrganizationChart(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteOrganizationChart(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Obtener o listar las jerarquías",
            description = "Obtiene o lista las jerarquías de acuerdo a ciertas variables o parámetros" +
                    "Args: id: identificador de la jerarquías. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase JerarquiaEntity). " +
                    "Returns: Objeto o lista de objetos con información de las depéndencias. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"/hierarchy", "/hierarchy/{id}"})
    public ResponseEntity<?> getHierarchy(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(JerarquiaEntity.class);
        JerarquiaEntity filter = (JerarquiaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, jerarquiaService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Obtener o listar las dependencias",
            description = "Obtiene o lista las dependencias de acuerdo a ciertas variables o parámetros" +
                    "Args: id: identificador de la dependencia. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase DependenciaEntity). " +
                    "Returns: Objeto o lista de objetos con información de las depéndencias. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"/dependency", "/dependency/{id}"})
    public ResponseEntity<?> getDependency(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(DependenciaEntity.class);
        DependenciaEntity filter = (DependenciaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, dependenciaService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Eliminar una dependencia por su id",
            description = "Elimina una dependencia por su id." +
                    "Args: id: identificador de la dependencia a eliminar.")
    @DeleteMapping("/dependency/{id}")
    public ResponseEntity<?> deleteDependency(@PathVariable Long id) throws CiadtiException {
        dependenciaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Crear una jerarquia junto a una dependencia si esta es pasada",
            description = "Crea una jerarquia junto a una dependencia si esta es pasada. " +
                    "Args: hierarchyJSON: objeto con información de la jerarquia. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping("hierarchy")
    public ResponseEntity<?> create(@Valid @RequestParam("hierarchy") String hierarchyJSON,
                                    @RequestParam(value = "file", required = false) MultipartFile file) throws IOException, CiadtiException {
        ObjectMapper objectMapper = new ObjectMapper();
        JerarquiaEntity jerarquiaEntity = objectMapper.readValue(hierarchyJSON, JerarquiaEntity.class);

        Long lastOrder = jerarquiaService.findLastOrderByIdPadre(jerarquiaEntity.getIdPadre());
        jerarquiaEntity.setOrden(
            jerarquiaEntity.getOrden() != null
            ? jerarquiaEntity.getOrden()
            : (lastOrder != null ? lastOrder + 1 : 1)
        );

        if (file != null) {
            if(jerarquiaEntity.getDependencia() != null){
                jerarquiaEntity.getDependencia().setMimetype(file.getContentType());
                jerarquiaEntity.getDependencia().setIcono(file.getBytes());
            }
        }
        return new ResponseEntity<>(configurationMediator.createHierarchy(jerarquiaEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Actualizar una jerarquía junto a su dependencia si esta es definida.",
        description = "Actualiza una jerarquía junto a su dependencia si esta es definida. " +
                "Args: hierarchy: objeto con información de la jerarquía. " +
                "id: identificador de la jerarquía. " +
                "Returns: Objeto con la información asociada.")
    @PutMapping("/hierarchy/{id}")
    public ResponseEntity<?> update(@Valid @RequestParam("hierarchy") String hierarchyJSON,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    @PathVariable Long id) throws CiadtiException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JerarquiaEntity jerarquiaEntity = objectMapper.readValue(hierarchyJSON, JerarquiaEntity.class);

        JerarquiaEntity jerarquiaEntityBD = jerarquiaService.findById(id);
        Long previusOrder = jerarquiaEntityBD.getOrden();
        Long lastOrder = jerarquiaService.findLastOrderByIdPadre(jerarquiaEntity.getIdPadre());
        jerarquiaEntityBD.setOrden(
            jerarquiaEntity.getOrden() != null
            ? jerarquiaEntity.getOrden()
            : (lastOrder != null ? lastOrder + 1 : 1)
        );
        if(jerarquiaEntity.getDependencia() != null){
            jerarquiaEntityBD.getDependencia().setNombre(jerarquiaEntity.getDependencia().getNombre());
            jerarquiaEntityBD.getDependencia().setDescripcion(jerarquiaEntity.getDependencia().getDescripcion());
            jerarquiaEntityBD.getDependencia().setIdConvencion(jerarquiaEntity.getDependencia().getIdConvencion());
            if(jerarquiaEntity.getDependencia().getIdConvencion() != null){
                ConvencionEntity convention = convencionService.findById(jerarquiaEntity.getDependencia().getIdConvencion());
                jerarquiaEntityBD.getDependencia().setConvencion(convention);
            }
        }

        if (file != null) {
            jerarquiaEntityBD.getDependencia().setMimetype(file.getContentType());
            jerarquiaEntityBD.getDependencia().setIcono(file.getBytes());
        }
        return new ResponseEntity<>(configurationMediator.updateHierarchy(jerarquiaEntityBD, previusOrder), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar una jerarquía por su id",
            description = "Elimina una jerarquía y sus subjerarquías por su id." +
                    "Args: id: identificador de la jerarquía a eliminar.")
    @DeleteMapping("/hierarchy/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteHierarchy(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar una jerarquía por su id",
            description = "Elimina una jerarquía y sus subjerarquías por su id." +
                    "Args: id: identificador de la jerarquía a eliminar.")
    @DeleteMapping("/hierarchy/with-dependency/{hierarchyId}")
    public ResponseEntity<?> deleteWithDependency(@PathVariable Long hierarchyId) throws CiadtiException {
        configurationMediator.deleteDependencyWithHierarchy(hierarchyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(
        summary = "Obtener las gestiones operativas asignadas a una dependencia en un organigrama a través del id de la jerarquía. ",
        description = "Obtiene las gestiones operativas asignadas a una dependencia en un organigrama a través del id de la jerarquía. " +
            "Args: hierarchyId: Identificador de la jerarquía. " +
            "Returns: Objeto con la información asociada."
    )
    @GetMapping("/operational-management/assigned")
    public ResponseEntity<?> getOperationalManagementByHierarchy(@RequestParam(name = "hierarchyId", required = true) Long hierarchyId) throws CiadtiException {
        return new ResponseEntity<>(gestionOperativaService.findAssignedOperationalsManagements(hierarchyId), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener las gestiones operativas que no han sido asignadas en un organigrama. ",
        description = "Obtiene las gestiones operativas que no han sido asignadas en un organigrama. " +
            "Args: organizationalChartId: Identificador del organigrama. " +
            "Returns: Objeto con la información asociada."
    )
    @GetMapping("/operational-management/no-assigned")
    public ResponseEntity<?> getAssignedOperationalsManagements(@RequestParam(name="organizationalChartId", required = true) Long organizationalChartId) throws CiadtiException {
    return new ResponseEntity<>(gestionOperativaService.findNoAssignedOperationalsManagements(organizationalChartId), HttpStatus.OK);
    }

    @Operation(
        summary = "Crear la relación de una dependencia en un organigrama (a través de la jerarquía) con ciertas gestiones operativas. ",
        description = "Crea la relación de una dependencia en un organigrama (a través de la jerarquía) con ciertas gestiones operativas. " +
            "Args: operationalManagementIds: Lista de identificadores de gestiones operativas. "+
            "hierarchyId: identificador de la jerarquía. " +
            "Return: Lista de relaciones."
    )
    @PostMapping("/operational-management")
    public ResponseEntity<?> createOperationalManagementHierarchy(@RequestBody List<Long> operationalManagementIds, @RequestParam(required = true) Long hierarchyId)  throws CiadtiException {
        return new ResponseEntity<>(configurationMediator.createOperationalManagementHierarchy(operationalManagementIds, hierarchyId), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar lista de relaciones de gestiones operativas con una jerarquía (define la dependencia en un organigrama) por su id",
        description = "Elimina lista de relaciones de gestiones operativas con una jerarquía." +
                "Args: id: identificador de la jerarquía a eliminar.")
    @DeleteMapping("/operational-management")
    public ResponseEntity<?> deleteHierarchyRelationshipWithOperationalsManagements(@RequestBody List<Long> relationshipIds) throws CiadtiException {
        configurationMediator.deleteHierarchyRelationshipWithOperationalsManagements(relationshipIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Eliminar la relación de una gestión operativa con una jerarquía (define la dependencia en un organigrama) por su id",
        description = "Elimina la relación de una gestión operativa con una jerarquía. " +
                "Args: relationshipId: identificador de la relación a eliminar (es lo mismo que idJerarquiaGestionOperativa).")
    @DeleteMapping("/operational-management/{relationshipId}")
    public ResponseEntity<?> deleteHierarchyRelationshipWithOperationalManagement(@PathVariable Long relationshipId) throws CiadtiException {
        configurationMediator.deleteHierarchyRelationshipWithOperationalManagement(relationshipId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @SuppressWarnings("null")
    @GetMapping("report")
    public ResponseEntity<?> downloadReportExcel(@RequestParam(name = "type", required = false) String type,
                                                 @RequestParam(name = "organizationChartId", required = false) Long organizationChartId) throws Exception {
        byte[] fileBytes = null;
        String extension = null;
        String mediaType = null;

        if ("excel".equalsIgnoreCase(type)) {
            extension = "xlsx";
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            fileBytes = organizationChartReportExcelJXLS.generate(organizationChartId);
        }else if ("flat-excel".equalsIgnoreCase(type)) {
            extension = "xlsx";
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            fileBytes = organizationChartReportPlainedExcelJXLS.generate(organizationChartId);
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
}
