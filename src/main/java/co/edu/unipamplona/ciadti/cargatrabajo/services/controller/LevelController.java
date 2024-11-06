package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EscalaSalarialEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EscalaSalarialService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/level")
public class LevelController {
    private final NivelService nivelService;
    private final EscalaSalarialService escalaSalarialService;
    private final ConfigurationMediator configurationMediator;

    @Operation(
        summary = "Obtener o listar los niveles de ocupación",
        description = "Obtiene lista de los niveles de ocupación de a cuerdo a ciertas variables o parámetros" +
                "Args: id: identificador del nivel de ocupación. " +
                "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase NivelEntity). " +
                "Returns: Objeto o lista de objetos con información de los niveles. " +
                "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> getLevel(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(NivelEntity.class);
        NivelEntity filter = (NivelEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);

        if (id != null){
            NivelEntity nivel = nivelService.findById(id);
            nivel.setEscalasSalariales(escalaSalarialService.findAllFilteredBy(EscalaSalarialEntity.builder().idNivel(id).build()));
            return new ResponseEntity<>(nivel, HttpStatus.OK);
        }
        return Methods.getResponseAccordingToId(id, nivelService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear un nivel ocupacional",
            description = "Crea un nivel ocupacional" +
                    "Args: nivelEntity: objeto con información del nivel ocupacional a registrar." +
                    "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> createLevel(@Valid @RequestBody NivelEntity nivelEntity) throws CiadtiException {
        return new ResponseEntity<>(configurationMediator.saveLevel(nivelEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un nivel de ocupación",
            description = "Actualiza un nivel de ocupación. " +
                    "Args: nivelEntity: objeto con información del nivel." +
                    "id: identificador del nivel." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLevel(@Valid @RequestBody NivelEntity nivelEntity, @PathVariable Long id) throws CiadtiException {
        nivelEntity.setId(id);
        return new ResponseEntity<>(configurationMediator.saveLevel(nivelEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar nivel de ocupación por el id",
            description = "Elimina un nivel de ocupación por su id." +
                    "Args: id: identificador del nivel a eliminar.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLevel(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteLevel(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar lista de niveles de ocupación por el id",
            description = "Elimina una lista de niveles de ocupación por su id." +
                    "Args: levelIds: identificadores de los niveles a eliminar.")
    @DeleteMapping
    public ResponseEntity<?> deleteLevels(@RequestBody List<Long> levelIds) throws CiadtiException {
        configurationMediator.deleteLevels(levelIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Eliminar escala salarial por el id",
            description = "Elimina una escala salarial por su id." +
                    "Args: id: identificador de la escala salarial a eliminar.")
    @DeleteMapping("/salary-scale/{id}")
    public ResponseEntity<?> deleteSalaryScale(@PathVariable Long id) throws CiadtiException{
        configurationMediator.deleteSalaryScale(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener o listar las escalas salariales",
        description = "Obtiene la lista de las escalas salariales de acuerdo con ciertas variables o parámetros" +
                "Args: id: identificador de la escala salarial. " +
                "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase EscalaSalarialEntity). " +
                "Returns: Objeto o lista de objetos con información de las escalas salariales. " +
                "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"/salary-scale", "/salary-scale/{id}"})
    public ResponseEntity<?> getSalaryScale(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(EscalaSalarialEntity.class);
        EscalaSalarialEntity filter = (EscalaSalarialEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, escalaSalarialService.findAllFilteredBy(filter));
    }
}
