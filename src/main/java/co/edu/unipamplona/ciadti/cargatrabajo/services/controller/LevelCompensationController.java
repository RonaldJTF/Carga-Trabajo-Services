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

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CompensacionLabNivelVigValorService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CompensacionLabNivelVigenciaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/level-compensation")
public class LevelCompensationController {
    private final CompensacionLabNivelVigValorService compensacionLabNivelVigValorService;
    private final CompensacionLabNivelVigenciaService compensacionLabNivelVigenciaService;
    private final ConfigurationMediator configurationMediator;

    @Operation(
            summary = "Obtener la relación de un compensación con un nivel en una vigencia",
            description = "Obtiene o lista las relaciones de una compensación laboral con los nivel ocupacional en una vigencia" +
                    "de acuerdo a ciertos parámetros o variables de filtrado" +
                    "Args: id: identificador del la compensación laboral. " +
                    "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase CompensacionLabNivelVigenciaEntity). " +
                    "Returns: Objeto o lista de objetos con información de la compensación laboral. " +
                    "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> getCompensacionLaboralNivelVigencia(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(CompensacionLabNivelVigenciaEntity.class);
        CompensacionLabNivelVigenciaEntity filter = (CompensacionLabNivelVigenciaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        List<CompensacionLabNivelVigenciaEntity> result = compensacionLabNivelVigenciaService.findAllBy(filter);
        if (id != null && result.size() > 0){
            CompensacionLabNivelVigenciaEntity clnv = result.get(0);
            clnv.setValoresCompensacionLabNivelVigencia(configurationMediator.findValuesByRulesOfLevelCompensation(clnv.getId()));
            return new ResponseEntity<>(clnv, HttpStatus.OK);
        }
        return Methods.getResponseAccordingToId(id, result);
    }

    @Operation(
        summary = "Crear una asociación de compensación laboral con un nivel en una vigencia y los respectivos valores que puede tomar por regla",
        description = "Crea una asociación de compensación laboral con un nivel en una vigencia y los respectivos valores que puede tomar por regla. " +
            "Args: compensacionLaboralEntity: objeto con información de la relación. " +
            "Returns: Objeto con la información asociada."
    )
    @PostMapping
    public ResponseEntity<?> createCompensacionLaboralNivelVigencia(@Valid @RequestBody CompensacionLabNivelVigenciaEntity compensacionLabNivelVigenciaEntity) throws CiadtiException {
        return new ResponseEntity<>(configurationMediator.saveLevelCompensation(compensacionLabNivelVigenciaEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una compensación laboral",
            description = "Actualiza una compensación laboral. " +
                    "Args: compensacionLaboralEntity: objeto con información de la compensación laboral. " +
                    "id: identificador de la compensación laboral. " +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompensacionLaboralNivelVigencia(@Valid @RequestBody CompensacionLabNivelVigenciaEntity compensacionLabNivelVigenciaEntity, @PathVariable Long id) throws CiadtiException {
        compensacionLabNivelVigenciaEntity.setId(id);
        return new ResponseEntity<>(configurationMediator.saveLevelCompensation(compensacionLabNivelVigenciaEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar compensación laboral en un nivel ocupacional para una vigencia por el id",
            description = "Elimina una compensación laboral en un nivel ocupacional para una vigencia por su id. " +
                    "Args: id: identificador de la compensación laboral en un nivel ocupacional para una vigencia a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteLevelCompensation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteLevelCompensations(@RequestBody List<Long> compensationIds) throws CiadtiException {
        configurationMediator.deleteLevelCompensations(compensationIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar un valor por regla para la relación de una compensación laboral en una vigencia para  un nivel.",
            description = "Elimina un valor por regla para la relación de una compensación laboral en una vigencia para  un nivel. " +
                    "Args: id: identificador del valor por regla  para la relación de una compensación laboral en una vigencia para un nivel a eliminar. ")
    @GetMapping("/value-by-rule")
    public ResponseEntity<?> getValuesByRuleByLevelCompensationId(@RequestParam("levelCompensationId") Long levelCompensationId) throws CiadtiException {
        return new ResponseEntity<>(configurationMediator.findValuesByRulesOfLevelCompensation(levelCompensationId), HttpStatus.OK);
    }

    @Operation(
            summary = "Eliminar un valor por regla para la relación de una compensación laboral en una vigencia para  un nivel.",
            description = "Elimina un valor por regla para la relación de una compensación laboral en una vigencia para  un nivel. " +
                    "Args: id: identificador del valor por regla  para la relación de una compensación laboral en una vigencia para un nivel a eliminar. ")
    @DeleteMapping("/value-by-rule/{id}")
    public ResponseEntity<?> deleteValueByRule(@PathVariable Long id) throws CiadtiException {
        compensacionLabNivelVigValorService.deleteByProcedure(id, RegisterContext.getRegistradorDTO().getJsonAsString());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener el valor de una variable para una regla que depende de la vigencia.",
        description = "Obtiene el valor de una variable para una regla que depende de la vigencia (en compensacionLabNivelVigencia). " +
                "Args: valueByRuleId: Identificador de la compensacionLabNivelVigValor que relaciona un valor para una regla " + 
                "en una vigencia para un nivel o escala salarial en una compensacion laboral. ")
    @GetMapping("/value-by-rule/value-in-validity")
    public ResponseEntity<?> getValueInValidityOfValueByRule(@RequestParam("valueByRuleId") Long valueByRuleId) {
        return new ResponseEntity<>(compensacionLabNivelVigValorService.getValueInValidityOfValueByRule(valueByRuleId), HttpStatus.OK);
    }
}
