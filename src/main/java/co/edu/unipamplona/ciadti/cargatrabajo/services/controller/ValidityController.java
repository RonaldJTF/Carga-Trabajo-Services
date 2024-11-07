package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.VigenciaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/validity")
public class ValidityController {
    private final VigenciaService vigenciaService;
    private final ConfigurationMediator configurationMediator;
    
    @Operation(
        summary = "Obtener o listar las vigencias con las variables parametrizadas con respectivos valores",
        description = "Obtiene o lista las vigencias con las variables parametrizadas con respectivos valores de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador de la vigencia. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase VigenciaEntity). " +
            "Returns: Objeto o lista de objetos con información de la vigencia. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(VigenciaEntity.class);
        VigenciaEntity filter = (VigenciaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, vigenciaService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear una vigencia",
        description = "Crea una vigencia" +
            "Args: vigenciaEntity: objeto con información de la vigencia. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody VigenciaEntity vigenciaEntity) throws CiadtiException {
        return new ResponseEntity<>(configurationMediator.saveValidity(vigenciaEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar una vigencia",
        description = "Actualiza una vigencia. " + 
            "Args: vigenciaEntity: objeto con información de la vigencia. " +
            "id: identificador del cargo. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update (@Valid @RequestBody VigenciaEntity vigenciaEntity, @PathVariable Long id) throws CiadtiException{
        vigenciaEntity.setId(id);
        return new ResponseEntity<>(configurationMediator.saveValidity(vigenciaEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar una vigencia junto a las parametrizaciones de los valores de las variables en esa vigencia.",
        description = "Eliminar una vigencia junto a las parametrizaciones de los valores de las variables en esa vigencia." + 
            "Args: id: identificador de la vigencia a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteValidity(@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteValidity(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar vigencias por el id junto a las parametrizaciones de los valores de las variables en esas vigencias.",
            description = "Elimina lista de vigencias por su id junto a las parametrizaciones de los valores de las variables en esas vigencias." +
                    "Args: positionIds: identificadores de las vigencias a eliminar.")
    @DeleteMapping
    public ResponseEntity<?> deleteValidities(@RequestBody List<Long> validityIds) throws CiadtiException {
        configurationMediator.deleteValidities(validityIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Eliminar un valor de una variable en una vigencia",
        description = "Eliminar un valor d euna variable en una vigencia" + 
            "Args: id: identificador del valor vigencia a eliminar. ")
    @DeleteMapping("/value-in-validity/{id}")
    public ResponseEntity<?> deleteValueInValidity(@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteValueInValidity(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
