package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipoDocumentoEntity;
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
@RequestMapping("/api/document-type")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;
    private final ParameterConverter parameterConverter;

    @Operation(
        summary = "Obtener o listar los tipos de documentos",
        description = "Obtiene o lista los tipos de documentos de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador del tipo de documento. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase TipoDocumnetoEntity). " +
            "Returns: Objeto o lista de objetos con información de la persona. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value={"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        TipoDocumentoEntity filter = (TipoDocumentoEntity) parameterConverter.converter(request.getParameterMap(), TipoDocumentoEntity.class);
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, tipoDocumentoService.findAllFilteredBy(filter));
    }
    
}
