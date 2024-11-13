package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLaboralEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CompensacionLaboralService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/compensation")
public class CompensationController {
    private final CompensacionLaboralService compensacionLaboralService;
    private final ConfigurationMediator configurationMediator;

    @Operation(
            summary = "Obtener o listar las compensaciones laborales",
            description = "Obtiene o lista las compensaciones laborales de acuerdo a ciertas variables o parámetros. " +
                    "Args: id: identificador del la compensación laboral. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase CompensacionLaboralEntity). " +
                    "Returns: Objeto o lista de objetos con información de la compensación laboral. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(CompensacionLaboralEntity.class);
        CompensacionLaboralEntity filter = (CompensacionLaboralEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, compensacionLaboralService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crea una compensación laboral",
            description = "Crea una compensación laboral" +
                    "Args: compensacionLaboralEntity: objeto con información de la compensación laboral. " +
                    "Returns: Objeto con la información asociada."
    )
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CompensacionLaboralEntity compensacionLaboralEntity) {
        return new ResponseEntity<>(compensacionLaboralService.save(compensacionLaboralEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una compensación laboral",
            description = "Actualiza una compensación laboral. " +
                    "Args: compensacionLaboralEntity: objeto con información de la compensación laboral. " +
                    "id: identificador de la compensación laboral. " +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody CompensacionLaboralEntity compensacionLaboralEntity, @PathVariable Long id) throws CiadtiException {
        CompensacionLaboralEntity compensacionLaboralDB = compensacionLaboralService.findById(id);
        if (compensacionLaboralDB != null) {
            compensacionLaboralEntity.setId(compensacionLaboralDB.getId());
        }
        return new ResponseEntity<>(compensacionLaboralService.save(compensacionLaboralEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar compensación laboral por el id",
            description = "Elimina una compensación laboral por su id. " +
                    "Args: id: identificador de la compensación laboral a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteCompensation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCompensations(@RequestBody List<Long> compensationIds) throws CiadtiException {
        configurationMediator.deleteCompensations(compensationIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
