package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GeneroService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.RolService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipoDocumentoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class BasicInformationController {

    private final TipoDocumentoService tipoDocumentoService;
    private final NivelService nivelService;
    private final GeneroService generoService;
    private final RolService rolService;
    private final TipologiaService tipologiaService;
    private final EstructuraService estructuraService;

    @Operation(
        summary = "Obtener o listar los tipos de documentos",
        description = "Obtiene o lista los tipos de documentos de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador del tipo de documento. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase TipoDocumnetoEntity). " +
            "Returns: Objeto o lista de objetos con información de la persona. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value={"document-type", "document-type/{id}"})
    public ResponseEntity<?> getDocumentType(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(TipoDocumentoEntity.class);
        TipoDocumentoEntity filter = (TipoDocumentoEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, tipoDocumentoService.findAllFilteredBy(filter));
    }
    

    @GetMapping(value = {"gender", "gender/{id}"})
    public ResponseEntity<?> getGender(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(GeneroEntity.class);
        GeneroEntity filter = (GeneroEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, generoService.findAllFilteredBy(filter));
    }

    @GetMapping(value = {"level", "level/{id}"})
    public ResponseEntity<?> getLevel(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(NivelEntity.class);
        NivelEntity filter = (NivelEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, nivelService.findAllFilteredBy(filter));
    }
    
    @GetMapping(value = {"role", "role/{id}"})
    public ResponseEntity<?> getRole(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(RolEntity.class);
        RolEntity filter = (RolEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, rolService.findAllFilteredBy(filter));
    }

    @GetMapping(value = {"inventory", "inventory/{id}"})
    public ResponseEntity<?> getInventory(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        return Methods.getResponseAccordingToId(id, tipologiaService.findInventarioTipologia());
    }

    @GetMapping(value = {"statistics", "statistics/{id}"})
    public ResponseEntity<?> getStatistics(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(EstructuraEntity.class);
        EstructuraEntity filter = (EstructuraEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return new ResponseEntity<>(estructuraService.statisticsDependence(filter), HttpStatus.OK);
    }

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(@RequestBody(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(PersonaEntity.class);
        PersonaEntity filter = (PersonaEntity) parameterConverter.converter(request.getParameterMap());
        System.out.println(filter);
        return Methods.getResponseAccordingToId(id, null);
    }

}
