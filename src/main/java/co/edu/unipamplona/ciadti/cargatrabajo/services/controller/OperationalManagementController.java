package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
        TipologiaEntity secondTypology = firstTypology.getTipologiaSiguiente();
        gestionOperativaEntity.setIdTipologia(gestionOperativaEntity.getIdTipologia() == null ? secondTypology.getId() : gestionOperativaEntity.getIdTipologia());
        gestionOperativaEntity.setTipologia(tipologiaService.findById(gestionOperativaEntity.getIdTipologia()));

        Long lastOrder = gestionOperativaService.findLastOrderByIdPadre(gestionOperativaEntity.getIdPadre());
        gestionOperativaEntity.setOrden(
            gestionOperativaEntity.getOrden() != null
            ? gestionOperativaEntity.getOrden()
            : (lastOrder != null ? lastOrder + 1 : 1)
        );

        return new ResponseEntity<>(gestionOperativaService.save(gestionOperativaEntity), HttpStatus.CREATED);
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
        gestionOperativaEntityBD.setIdPadre(gestionOperativaEntity.getIdPadre());
        gestionOperativaEntityBD.setIdTipologia(gestionOperativaEntity.getIdTipologia());
        gestionOperativaEntityBD.setOrden(gestionOperativaEntity.getOrden());
        return new ResponseEntity<>(configurationMediator.updateOperationalManagement(gestionOperativaEntityBD, previusOrder), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar gestión operativa por el id",
        description = "Elimina una gestión operativa por su id." +
                "Args: id: identificador de la gestión operativa a eliminar.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOperationalManagement(@PathVariable Long id) throws CiadtiException {
        gestionOperativaService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/create-from-structure")
    public ResponseEntity<?> associateStructure(@RequestBody List<EstructuraEntity> structures){
        return new ResponseEntity<>(configurationMediator.createOperationalManagementsFromStructures(structures), HttpStatus.OK);
    }
}
