package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigValorEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CompensacionLabNivelVigenciaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ReglaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.VariableService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.GeneralExpressionMediator;
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
    private final CompensacionLabNivelVigenciaService compensacionLabNivelVigenciaService;
    private final VariableService variableService;
    private final ReglaService reglaService;
    private final GeneralExpressionMediator generalExpressionMediator;

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
        if(id != null){
            CompensacionLabNivelVigenciaEntity clnv = compensacionLabNivelVigenciaService.findById(id);
            for (CompensacionLabNivelVigValorEntity cnvv : clnv.getValoresCompensacionLabNivelVigencia()){
                List<VariableEntity> includedVariablesInValue = variableService.findAllIncludedVariablesInVariable(cnvv.getIdVariable());
                List<VariableEntity> includedVariablesInRule = variableService.findAllIncludedVariablesInRule(cnvv.getIdRegla());
                cnvv.getVariable().setExpresionValor(generalExpressionMediator.getExpressionWithVariableNames(cnvv.getVariable().getValor(), includedVariablesInValue));
                cnvv.getRegla().setExpresionCondiciones(generalExpressionMediator.getExpressionWithVariableNames(cnvv.getRegla().getCondiciones(), includedVariablesInRule));
            }
            return new ResponseEntity<>(clnv, HttpStatus.OK);
        }else{
            ParameterConverter parameterConverter = new ParameterConverter(CompensacionLabNivelVigenciaEntity.class);
            CompensacionLabNivelVigenciaEntity filter = (CompensacionLabNivelVigenciaEntity) parameterConverter.converter(request.getParameterMap());
            return Methods.getResponseAccordingToId(id, compensacionLabNivelVigenciaService.findAllFilteredBy(filter));
        }
    }

    @Operation(
            summary = "Crea una compensación laboral",
            description = "Crea una compensación laboral" +
                    "Args: compensacionLaboralEntity: objeto con información de la compensación laboral. " +
                    "Returns: Objeto con la información asociada."
    )
    @PostMapping
    public ResponseEntity<?> createCompensacionLaboralNivelVigencia(@Valid @RequestBody CompensacionLabNivelVigenciaEntity compensacionLabNivelVigenciaEntity) {
        return new ResponseEntity<>(compensacionLabNivelVigenciaService.save(compensacionLabNivelVigenciaEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una compensación laboral",
            description = "Actualiza una compensación laboral. " +
                    "Args: compensacionLaboralEntity: objeto con información de la compensación laboral. " +
                    "id: identificador de la compensación laboral. " +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompensacionLaboralNivelVigencia(@Valid @RequestBody CompensacionLabNivelVigenciaEntity compensacionLabNivelVigenciaEntity, @PathVariable Long id) throws CiadtiException {
        CompensacionLabNivelVigenciaEntity clnvBD = compensacionLabNivelVigenciaService.findById(id);
        clnvBD.setIdVigencia(compensacionLabNivelVigenciaEntity.getIdVigencia());
        clnvBD.setIdNivel(compensacionLabNivelVigenciaEntity.getIdNivel());
        clnvBD.setIdEscalaSalarial(compensacionLabNivelVigenciaEntity.getIdEscalaSalarial());
        clnvBD.setIdCompensacionLaboral(compensacionLabNivelVigenciaEntity.getIdCompensacionLaboral());
        return new ResponseEntity<>(compensacionLabNivelVigenciaService.save(clnvBD), HttpStatus.CREATED);
    }
}
