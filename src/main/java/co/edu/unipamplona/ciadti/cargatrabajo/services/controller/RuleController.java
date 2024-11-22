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
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ReglaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ReglaService;
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
@RequestMapping("/api/rule")
public class RuleController {
    private final ReglaService reglaService;
    private final VariableService variableService;
    private final ConfigurationMediator configurationMediator;
    private final GeneralExpressionMediator generalExpressionMediator;

    @Operation(
        summary = "Obtener o listar las reglas",
        description = "Obtiene o lista las reglas de acuerdo a ciertas variables o parámetros. " +
                "Args: id: identificador de la regla. " +
                "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase ReglaEntity). " +
                "Returns: Objeto o lista de objetos con información de la regla. " +
                "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
         if (id != null){
            return new ResponseEntity<>(configurationMediator.findRule(id), HttpStatus.OK);
        }else{
            ParameterConverter parameterConverter = new ParameterConverter(ReglaEntity.class);
            ReglaEntity filter = (ReglaEntity) parameterConverter.converter(request.getParameterMap());
            filter.setId(id == null ? filter.getId() : id);
            List<ReglaEntity> result = reglaService.findAllFilteredBy(filter);
            List<VariableEntity> variables = variableService.findAll();
            return Methods.getResponseAccordingToId(id, generalExpressionMediator.completeRuleInformation(result, variables));
        }
    }
    
    @Operation(
            summary = "Crear un tipo de cargo",
            description = "Crea un tipo de cargo" +
                    "Args: categoriaEntity: objeto con información del tipo de cargo a registrar. " +
                    "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ReglaEntity reglaEntity) {
        ReglaEntity reglaNew = new ReglaEntity();
        reglaNew.setNombre(reglaEntity.getNombre());
        reglaNew.setDescripcion(reglaEntity.getDescripcion());
        reglaNew.setCondiciones(reglaEntity.getCondiciones()); 
        reglaNew.setGlobal(reglaEntity.getGlobal()); 
        reglaNew.setEstado(reglaEntity.getEstado()); 
        return new ResponseEntity<>(reglaService.save(reglaNew), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar una regla",
        description = "Actualiza una regla. " + 
            "Args: reglaEntity: objeto con información del regla. " +
            "id: identificador de la regla. " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update (@Valid @RequestBody ReglaEntity reglaEntity, @PathVariable Long id) throws CiadtiException{
        ReglaEntity reglaEntityBD = reglaService.findById(id);
        reglaEntityBD.setNombre(reglaEntity.getNombre());
        reglaEntityBD.setDescripcion(reglaEntity.getDescripcion());
        reglaEntityBD.setCondiciones(reglaEntity.getCondiciones()); 
        reglaEntityBD.setGlobal(reglaEntity.getGlobal()); 
        reglaEntityBD.setEstado(reglaEntity.getEstado()); 
        reglaService.save(reglaEntityBD);
        List<VariableEntity> includedVariablesInRule = variableService.findAllIncludedVariablesInRule(reglaEntityBD.getId());
        reglaEntityBD.setExpresionCondiciones(generalExpressionMediator.getExpressionWithVariableNames(reglaEntityBD.getCondiciones(), includedVariablesInRule));
        return new ResponseEntity<>(reglaEntityBD, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar una regla",
        description = "Elimina una regla" + 
            "Args: id: identificador de la regla a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete (@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteRule(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar reglas por el id",
            description = "Elimina lista de reglas por su id." +
                    "Args: ruleIds: identificadores de los tipos de reglas a eliminar.")
    @DeleteMapping
    public ResponseEntity<?> deleteRules(@RequestBody List<Long> ruleIds) throws CiadtiException {
        configurationMediator.deleteRules(ruleIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener la lista de reglas que están activas y son globales. ",
        description = "Obtiene la lista de reglas que están activas y son globales, pero también obtiene las reglas activas " +
            "que no se ha configurado como globales y que se han asociado al nivel y que están activas. " +
            "Nota: Tambien se traen las reglas que se han definido como no globales y que no se han relacionado a ningún nivel en compensacionLabNivenVigencia."
    )
    @GetMapping("/active")
    public ResponseEntity<?> getActiveRules(@RequestParam Long levelId) throws CiadtiException {
        List<ReglaEntity> result = reglaService.getGlobalAndLevelActiveRules(levelId);
        List<VariableEntity> variables = variableService.findAll();
        return new ResponseEntity<>(generalExpressionMediator.completeRuleInformation(result, variables), HttpStatus.OK);
    }
}
