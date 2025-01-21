package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.JerarquiaGestionOperativaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaGestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final ConfigurationMediator configurationMediator;
    private final JerarquiaGestionOperativaDAO jerarquiaGestionOperativaDAO;

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

    @PostMapping("/create-from-structure")
    public ResponseEntity<?> associateStructure(@RequestBody List<EstructuraEntity> structures){
        return new ResponseEntity<>(configurationMediator.createOperationalManagementsFromStructures(structures), HttpStatus.OK);
    }


    @PostMapping("/migrate-structures")
    public ResponseEntity<?> migrateStructures(@RequestBody ArrayList<EstructuraEntity> estructuras, @RequestParam(required = false) Long idParent) throws CiadtiException {
        return new ResponseEntity<>(configurationMediator.migrateStructures(estructuras, idParent), HttpStatus.CREATED);
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
    
}
