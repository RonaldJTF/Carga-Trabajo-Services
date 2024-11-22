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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.VariableService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.GeneralExpressionMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/variable")
public class VariableController {

    private final VariableService variableService;
    private final ConfigurationMediator configurationMediator;
    private final GeneralExpressionMediator generalExpressionMediator;

    @Operation(
        summary = "Obtener o listar las variables",
        description = "Obtiene o lista las variables de las compensaciones laborales de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador de la variable. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase VariableEntity). " +
            "Returns: Objeto o lista de objetos con información de la variable. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        if (id != null){
            return new ResponseEntity<>(configurationMediator.findVariable(id), HttpStatus.OK);
        }else{
            ParameterConverter parameterConverter = new ParameterConverter(VariableEntity.class);
            VariableEntity filter = (VariableEntity) parameterConverter.converter(request.getParameterMap());
            filter.setId(id==null ? filter.getId() : id);
            List<VariableEntity> result = variableService.findAllFilteredBy(filter);
            List<VariableEntity> variables = variableService.findAll();
            return Methods.getResponseAccordingToId(id, generalExpressionMediator.completeVariableInformation(result, variables));
        }
    }

    @Operation(
        summary="Crear una variable",
        description = "Crea una variable" +
            "Args: variableEntity: objeto con información de la variable. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> createVariable(@Valid @RequestBody VariableEntity variableEntity) {
        return new ResponseEntity<>(variableService.save(variableEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar una variable",
        description = "Actualiza una variable. " + 
            "Args: variableEntity: objeto con información de la variable. " +
            "id: identificador del cargo. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariable(@Valid @RequestBody VariableEntity variableEntity, @PathVariable Long id) throws CiadtiException{
        VariableEntity variableEntityDB = variableService.findById(id);
        variableEntityDB.setNombre(variableEntity.getNombre());
        variableEntityDB.setDescripcion(variableEntity.getDescripcion());
        variableEntityDB.setValor(variableEntity.getValor());
        variableEntityDB.setPrimaria(variableEntity.getPrimaria());
        variableEntityDB.setGlobal(variableEntity.getGlobal());
        variableEntityDB.setPorVigencia(variableEntity.getPorVigencia());
        variableEntityDB.setEstado(variableEntity.getEstado());

        if (Methods.convertToBoolean(variableEntity.getPorVigencia())){
            variableEntityDB.setValor("");
        }
        variableService.save(variableEntityDB);
        List<VariableEntity> includedVariablesInValue = variableService.findAllIncludedVariablesInVariable(variableEntityDB.getId());
        variableEntityDB.setExpresionValor(generalExpressionMediator.getExpressionWithVariableNames(variableEntityDB.getValor(), includedVariablesInValue));
        return new ResponseEntity<>(variableEntityDB, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Elimina una variable",
        description = "Eliminar una variable" + 
            "Args: id: identificador de la variable a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariable(@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteVariable(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar variables por el id",
            description = "Elimina lista de variables por su id." +
                    "Args: variableIds: identificadores de los tipos de variables a eliminar.")
    @DeleteMapping
    public ResponseEntity<?> deleteVariables(@RequestBody List<Long> validityIds) throws CiadtiException {
        configurationMediator.deleteVariables(validityIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(
        summary = "Obtener las variables que son configuradas por vigencias y que se encuentran activas",
        description = "Obtiene todas las variables que dependen de una vigencia, por ejemplo el salario mínimo, y que estan activas"
    )
    @GetMapping(value = "/configured-by-validity")
    private ResponseEntity<?> getVariablesOfValidity(){
        return new ResponseEntity<>(variableService.findAllConfigureByValidityAndActive(), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener la lista de variables que están activas, son globales y no son primarias. ",
        description = "Obtiene la lista de variables que están activas, son globales y no son primarias, pero también obtiene " +
            "las variables activas que no se han configurado como globales y que se han asociado al nivel y que están activas. " +
            "Nota: Tambien se traen las variables que se han definido como no globales y que no se han relacionado a ningún nivel en compensacionLabNivenVigencia."
    )
    @GetMapping("/active")
    public ResponseEntity<?> getActiveRules(@RequestParam Long levelId) throws CiadtiException {
        List<VariableEntity> variables = variableService.findAll();
        List<VariableEntity> result = variableService.getGlobalAndNoPrimaryAndLevelActiveVariables(levelId);
        return new ResponseEntity<>(generalExpressionMediator.completeVariableInformation(result, variables), HttpStatus.OK);
    }
}
