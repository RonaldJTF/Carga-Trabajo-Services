package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.OrganigramaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.OrganigramaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization-chart")
public class OrganizationStructureController {
    private final OrganigramaService organigramaService;

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
        organigramaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
