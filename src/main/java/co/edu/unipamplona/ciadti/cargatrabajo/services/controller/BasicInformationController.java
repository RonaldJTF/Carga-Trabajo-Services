package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GeneroEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipoDocumentoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GeneroService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipoDocumentoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class BasicInformationController {

    private final TipoDocumentoService tipoDocumentoService;
    private final NivelService nivelService;
    private final ParameterConverter parameterConverter;
    private final GeneroService generoService;

    @Operation(
        summary = "Obtener o listar los tipos de documentos",
        description = "Obtiene o lista los tipos de documentos de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador del tipo de documento. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase TipoDocumnetoEntity). " +
            "Returns: Objeto o lista de objetos con información de la persona. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value={"document-type", "document-type/{id}"})
    public ResponseEntity<?> getDocumentType(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        TipoDocumentoEntity filter = (TipoDocumentoEntity) parameterConverter.converter(request.getParameterMap(), TipoDocumentoEntity.class);
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, tipoDocumentoService.findAllFilteredBy(filter));
    }
    

    @GetMapping(value = {"gender", "gender/{id}"})
    public ResponseEntity<?> getGender(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        GeneroEntity filter = (GeneroEntity) parameterConverter.converter(request.getParameterMap(), GeneroEntity.class);
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, generoService.findAllFilteredBy(filter));
    }

    @GetMapping(value = {"level", "level/{id}"})
    public ResponseEntity<?> getLevel(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        NivelEntity filter = (NivelEntity) parameterConverter.converter(request.getParameterMap(), NivelEntity.class);
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, nivelService.findAllFilteredBy(filter));
    }
    
}
