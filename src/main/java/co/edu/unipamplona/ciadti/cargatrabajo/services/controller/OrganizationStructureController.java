package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ConvencionEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DependenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ConvencionService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.DependenciaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.JerarquiaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization-chart")
public class OrganizationStructureController {
    private final OrganigramaService organigramaService;
    private final JerarquiaService jerarquiaService;
    private final DependenciaService dependenciaService;
    private final ConvencionService convencionService;
    private final ConfigurationMediator configurationMediator;
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
                    "Args: organigramaEntity: objeto con información del organigrama a registrar." +
                    "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> createOrganizationChart(@Valid @RequestBody OrganigramaEntity organigramaEntity) {
        return new ResponseEntity<>(organigramaService.save(organigramaEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un organigrama",
            description = "Actualiza un organigrama. " +
                    "Args: gestionOperativaEntity: objeto con información del organigrama." +
                    "id: identificador del organigrama." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrganizationChart(@Valid @RequestBody OrganigramaEntity organigramaEntity, @PathVariable Long id) throws CiadtiException {
        OrganigramaEntity organigramaEntityBD = organigramaService.findById(id);
        organigramaEntityBD.setDescripcion(organigramaEntity.getDescripcion());
        organigramaEntityBD.setNombre(organigramaEntity.getNombre());
        organigramaEntityBD.setIdNormatividad(organigramaEntity.getIdNormatividad());
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


    @PostMapping("/operational-management")
    public ResponseEntity<?> createOperationalManagementHierarchy(@RequestBody List<GestionOperativaEntity> operationalManagements, @RequestParam(required = true) Long hierarchyId)  throws CiadtiException {
        return new ResponseEntity<>(configurationMediator.createOperationalManagementHierarchy(operationalManagements, hierarchyId), HttpStatus.CREATED);
    }

    @GetMapping("/operational-management")
    public ResponseEntity<?> getOperationalManagementByHierarchy(@RequestParam(required = true) Long hierarchyId) throws CiadtiException {
        List<GestionOperativaEntity> gestiones = gestionOperativaService.findOperationalManagementByHierarchy(hierarchyId);
    return new ResponseEntity<>(gestiones, HttpStatus.OK);
    }

    @DeleteMapping("/operational-management")
    public ResponseEntity<?> deleteOperationalManagementHierarchy(@RequestBody List<Long> hierarchyIds) throws CiadtiException {
        configurationMediator.deleteOperationalManagementHierarchy(hierarchyIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/operational-management-organization-chart")
    public ResponseEntity<?> findOperationalManagementByOrganizationalChart(@RequestParam(required = true) Long organizationalChartId) throws CiadtiException {
        List<GestionOperativaEntity> gestiones = gestionOperativaService.findOperationalManagementByOrganizationalChart(organizationalChartId);
    return new ResponseEntity<>(gestiones, HttpStatus.OK);
    }
}
