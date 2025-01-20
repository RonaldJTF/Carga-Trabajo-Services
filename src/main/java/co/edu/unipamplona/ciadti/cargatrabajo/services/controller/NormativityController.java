package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

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
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NormatividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NormatividadService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/normativity")
public class NormativityController {
    private final NormatividadService normatividadService;
    private final ConfigurationMediator configurationMediator;

    @Operation(
        summary = "Obtener o listar las normatividades",
        description = "Obtiene lista las normatividades de acuerdo a ciertas variables o parámetros" +
                "Args: id: identificador de la normatividad. " +
                "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase NormatividadEntity). " +
                "Returns: Objeto o lista de objetos con información de las normatividades. " +
                "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados.")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> getNormativity(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException {
        ParameterConverter parameterConverter = new ParameterConverter(NormatividadEntity.class);
        NormatividadEntity filter = (NormatividadEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id == null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, normatividadService.findAllFilteredBy(filter));
    }

    @Operation(
            summary = "Crear una normatividad",
            description = "Crea una normatividad" +
                    "Args: normatividadEntity: objeto con información de la normatividad a registrar." +
                    "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> createNormativity(@Valid @RequestBody NormatividadEntity normatividadEntity) {
        return new ResponseEntity<>(normatividadService.save(normatividadEntity), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar una normatividad",
            description = "Actualiza una normatividad. " +
                    "Args: normatividadEntity: objeto con información de la normatividad." +
                    "id: identificador de la normatividad." +
                    "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNormativity(@Valid @RequestBody NormatividadEntity normatividadEntity, @PathVariable Long id) throws CiadtiException {
        NormatividadEntity normatividadEntityBD = normatividadService.findById(id);
        normatividadEntityBD.setDescripcion(normatividadEntity.getDescripcion());
        normatividadEntityBD.setNombre(normatividadEntity.getNombre());
        normatividadEntityBD.setEmisor(normatividadEntity.getEmisor());
        normatividadEntityBD.setFechaInicioVigencia(normatividadEntity.getFechaInicioVigencia());
        normatividadEntityBD.setFechaFinVigencia(normatividadEntity.getFechaFinVigencia());
        normatividadEntityBD.setEstado(normatividadEntity.getEstado());
        normatividadEntityBD.setEsEscalaSalarial(normatividadEntity.getEsEscalaSalarial());
        normatividadEntityBD.setIdAlcance(normatividadEntity.getIdAlcance());
        normatividadEntityBD.setIdTipoNormatividad(normatividadEntity.getIdTipoNormatividad());
        return new ResponseEntity<>(configurationMediator.updateNormativity(normatividadEntityBD), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar normatividad por el id junto a sus escalas salariales",
            description = "Elimina una normatividad por su id junto a sus escalas salariales." +
                    "Args: id: identificador de la normstividad a eliminar.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNormativity(@PathVariable Long id) throws CiadtiException {
        configurationMediator.deleteNormativity(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener o listar las normatividades generales, es decir las que no tienen "+ 
                "asociadas un alcance y no son de escalas salariales",
        description = "Obtiene lista las normatividades generales de acuerdo al estado. " +
                "status:Estado de la normatividad '0' o '1' o si no se define entonces no filtra por el estado. " +
                "Returns: Objeto o lista de objetos con información de las normatividades. ")
    @GetMapping("/general")
    public ResponseEntity<?> getGeneralNormativities(@RequestParam(required = false, name = "status") String status){
        return new ResponseEntity<>(normatividadService.findGeneralNormativities(status), HttpStatus.OK);
    }
}
