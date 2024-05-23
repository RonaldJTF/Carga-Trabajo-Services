package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PersonaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/person")
public class PersonaController {
    private final PersonaService personaService;
    private final ConfigurationMediator configurationMediator;

    @Operation(
        summary = "Obtener o listar las personas",
        description = "Obtiene o lista las personas de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador de la persona. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase PersonaEntity). " +
            "Returns: Objeto o lista de objetos con información de la persona. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value={"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        ParameterConverter parameterConverter = new ParameterConverter(PersonaEntity.class);
        PersonaEntity filter = (PersonaEntity) parameterConverter.converter(request.getParameterMap());
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, personaService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear una persona",
        description = "Crea una persona" + 
            "Args: personaEntity: objeto con información de la persona. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestParam(value = "person") String personaJSON, 
                                    @RequestParam (value = "file", required = false) MultipartFile photoFile) throws IOException, CloneNotSupportedException{
        ObjectMapper objectMapper = new ObjectMapper();
        PersonaEntity personaEntity = objectMapper.readValue(personaJSON, PersonaEntity.class);
        return new ResponseEntity<>(configurationMediator.savePerson(personaEntity, photoFile), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar una persona",
        description = "Actualiza una persona. " + 
            "Args: personaEntity: objeto con información de la persona. " +
            "id: identificador de la persona. " +
            "photoFile: archivo de la foto de perfil " +
            "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestParam (value = "person") String personaJSON, 
                                    @RequestParam(value = "file", required = false) MultipartFile photoFile) throws IOException, CloneNotSupportedException, CiadtiException{
        ObjectMapper objectMapper = new ObjectMapper();
        PersonaEntity personaEntity = objectMapper.readValue(personaJSON, PersonaEntity.class);
        personaEntity.setId(id);
        
        PersonaEntity personaEntityBD = personaService.findById(id);
        personaEntityBD.setCorreo(personaEntity.getCorreo());   
        personaEntityBD.setDocumento(personaEntity.getDocumento());   
        personaEntityBD.setIdGenero(personaEntity.getIdGenero());   
        personaEntityBD.setIdTipoDocumento(personaEntity.getIdTipoDocumento());    
        personaEntityBD.setPrimerNombre(personaEntity.getPrimerNombre()); 
        personaEntityBD.setSegundoNombre(personaEntity.getSegundoNombre());   
        personaEntityBD.setPrimerApellido(personaEntity.getPrimerApellido());   
        personaEntityBD.setSegundoApellido(personaEntity.getSegundoApellido());  
        personaEntityBD.setTelefono(personaEntity.getTelefono());   
        return new ResponseEntity<>(configurationMediator.savePerson(personaEntity, photoFile), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar persona por el id",
        description = "Elimina una persona por su id. " + 
            "Args: id: identificador de la persona a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        configurationMediator.deletePerson(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deletePeople(@RequestBody List<Long> personIds) throws CiadtiException {
        configurationMediator.deletePeople(personIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
