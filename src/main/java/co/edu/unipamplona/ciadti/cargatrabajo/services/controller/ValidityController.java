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
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ValorVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ValorVigenciaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.VariableService;
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
    private final ValorVigenciaService valorVigenciaService;
    private final ConfigurationMediator configurationMediator;
    private final VariableService variableService;
    
    @Operation(
        summary = "Obtener o listar las vigencias",
        description = "Obtiene o lista las vigencias de acuerdo a ciertas variables o parámetros. " +
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
    public ResponseEntity<?> create(@Valid @RequestBody VigenciaEntity vigenciaEntity) {
        VigenciaEntity vigenciaNew = new VigenciaEntity();
        vigenciaNew.setNombre(vigenciaEntity.getNombre());
        vigenciaNew.setAnio(vigenciaEntity.getAnio());
        vigenciaNew.setEstado(vigenciaEntity.getEstado());
        return new ResponseEntity<>(vigenciaService.save(vigenciaNew), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar una vigencia",
        description = "Actualiza una vigencia. " + 
            "Args: vigenciaEntity: objeto con información de la vigencia. " +
            "id: identificador del cargo. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update (@Valid @RequestBody VigenciaEntity vigenciaEntity, @PathVariable Long id) throws CiadtiException{
        VigenciaEntity vigenciaEntityBD = vigenciaService.findById(id);
        vigenciaEntityBD.setNombre(vigenciaEntity.getNombre());
        vigenciaEntityBD.setAnio(vigenciaEntity.getAnio());
        vigenciaEntityBD.setEstado(vigenciaEntity.getEstado());
        return new ResponseEntity<>(vigenciaService.save(vigenciaEntityBD), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar una vigencia",
        description = "Eliminar una vigencia" + 
            "Args: id: identificador de la vigencia a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteValidity(@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteValidity(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar posiciones por el id",
            description = "Elimina lista de posiciones por su id." +
                    "Args: positionIds: identificadores de los tipos de posiciones a eliminar.")
    @DeleteMapping
    public ResponseEntity<?> deleteValidities(@RequestBody List<Long> validityIds) throws CiadtiException {
        configurationMediator.deleteValidities(validityIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener o listar los valores de vigencias",
        description = "Obtiene o lista los valores de vigencias de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador del valor de vigencia. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase ValorVigenciaEntity). " +
            "Returns: Objeto o lista de objetos con información del valor de vigencia. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"/validity-value", "/validity-value/{id}"})
    public ResponseEntity<?> getValidityValue(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(ValorVigenciaEntity.class);
        ValorVigenciaEntity filter = (ValorVigenciaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, valorVigenciaService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear un valor de vigencia",
        description = "Crea un valor de vigencia" +
            "Args: valorVigenciaEntity: objeto con información del valor de vigencia. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping("/validity-value")
    public ResponseEntity<?> createValidityValue(@Valid @RequestBody ValorVigenciaEntity valorVigenciaEntity) {
        ValorVigenciaEntity valorVigenciaNew = new ValorVigenciaEntity();
        valorVigenciaNew.setValor(valorVigenciaEntity.getValor());
        return new ResponseEntity<>(valorVigenciaService.save(valorVigenciaNew), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar un valor de vigencia",
        description = "Actualiza un valor de vigencia. " + 
            "Args: valorVigenciaEntity: objeto con información del valor de vigencia. " +
            "id: identificador del cargo. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/validity-value/{id}")
    public ResponseEntity<?> updateValidityValue(@Valid @RequestBody ValorVigenciaEntity valorVigenciaEntity, @PathVariable Long id) throws CiadtiException{
        ValorVigenciaEntity valorVigenciaEntityBD = valorVigenciaService.findById(id);
        valorVigenciaEntityBD.setValor(valorVigenciaEntity.getValor());
        return new ResponseEntity<>(valorVigenciaService.save(valorVigenciaEntityBD), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar un valor de vigencia",
        description = "Eliminar un valor de vigencia" + 
            "Args: id: identificador del valor de vigencia a eliminar. ")
    @DeleteMapping("/validity-value/{id}")
    public ResponseEntity<?> deleteValidityValue(@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteValidityValue(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar valores de vigencia por el id",
            description = "Elimina lista de valores de vigencia por su id." +
                    "Args: validityValuesIds: identificadores de los tipos de posiciones a eliminar.")
    @DeleteMapping("/validity-value")
    public ResponseEntity<?> deleteValidityValues(@RequestBody List<Long> validityValueIds) throws CiadtiException {
        configurationMediator.deleteValidityValues(validityValueIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener o listar una variable",
        description = "Obtiene o lista las variables de acuerdo a ciertos parámetros. " +
            "Args: id: identificador de variable. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase VariableEntity). " +
            "Returns: Objeto o lista de objetos con información del valor de variable. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"/variable", "/variable/{id}"})
    public ResponseEntity<?> getVariable(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(VariableEntity.class);
        VariableEntity filter = (VariableEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, variableService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear una variable",
        description = "Crea una variable" +
            "Args: variableEntity: objeto con información de la variable. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping("/variable")
    public ResponseEntity<?> createVariable(@Valid @RequestBody VariableEntity variableEntity) {
        return new ResponseEntity<>(variableService.save(variableEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar una variable",
        description = "Actualiza una variable. " + 
            "Args: variableEntity: objeto con información de la variable. " +
            "id: identificador del cargo. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/variable/{id}")
    public ResponseEntity<?> updateVariable(@Valid @RequestBody VariableEntity variableEntity, @PathVariable Long id) throws CiadtiException{
        VariableEntity variableEntityDB = new VariableEntity();
        variableEntityDB.setNombre(variableEntity.getNombre());
        variableEntityDB.setDescripcion(variableEntity.getDescripcion());
        variableEntityDB.setValor(variableEntity.getValor());
        variableEntityDB.setPrimaria(variableEntity.getPrimaria());
        variableEntityDB.setGlobal(variableEntity.getGlobal());
        variableEntityDB.setPorVigencia(variableEntity.getPorVigencia());
        variableEntityDB.setEstado(variableEntity.getPorVigencia());
        return new ResponseEntity<>(variableService.save(variableEntityDB), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Elimina una variable",
        description = "Eliminar una variable" + 
            "Args: id: identificador de la variable a eliminar. ")
    @DeleteMapping("/variable/{id}")
    public ResponseEntity<?> deleteVariable(@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteVariable(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar variables por el id",
            description = "Elimina lista de variables por su id." +
                    "Args: variableIds: identificadores de los tipos de variables a eliminar.")
    @DeleteMapping("/variable")
    public ResponseEntity<?> deleteVariables(@RequestBody List<Long> validityIds) throws CiadtiException {
        configurationMediator.deleteVariables(validityIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
