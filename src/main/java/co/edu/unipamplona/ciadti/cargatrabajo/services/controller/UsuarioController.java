package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.cipher.CipherService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.converter.ParameterConverter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final ParameterConverter parameterConverter;
    private final ConfigurationMediator configurationMediator;
    private final PasswordEncoder passwordEncoder;
    private final CipherService cipherService;

    @Operation(
        summary = "Obtener o listar las usuarios",
        description = "Obtiene o lista los usuarios de acuerdo a ciertas variables o parámetros. " +
            "Args: id: identificador del usuario. " +
            "request: Usado para obtener los parámetros pasados y que serán usados para filtrar (Clase UsuarioEntity). " +
            "Returns: Objeto o lista de objetos con información del usuario. " +
            "Nota: Puede hacer uso de todos, de ninguno, o de manera combinada de las variables o parámetros especificados. ")
    @GetMapping(value = {"", "/{id}"})
    public ResponseEntity<?> get(@PathVariable(required = false) Long id, HttpServletRequest request) throws CiadtiException{
        UsuarioEntity filter = (UsuarioEntity) parameterConverter.converter(request.getParameterMap(), UsuarioEntity.class);
        filter.setId(id==null ? filter.getId() : id);
        return Methods.getResponseAccordingToId(id, usuarioService.findAllFilteredBy(filter));
    }

    @Operation(
        summary="Crear un usuario junto a los roles si estos son definidos",
        description = "Crea un usuario junto a los roles si estos son definidos" + 
            "Args: usuarioEntity: objeto con información del usuario. " +
            "Returns: Objeto con la información asociada.")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UsuarioEntity usuarioEntity) throws CloneNotSupportedException{
        String password = cipherService.decryptCredential(usuarioEntity.getPassword());
        usuarioEntity.setPassword(passwordEncoder.encode(password));
        return new ResponseEntity<>(configurationMediator.createUser(usuarioEntity), HttpStatus.CREATED);
    }

    @Operation(
        summary="Actualizar un usuario junto a los roles si estos son definidos",
        description = "Actualiza un usuario junto a los roles si estos son definidos" + 
            "Args: usuarioEntity: objeto con información del usuario. " +
            "id: identificador del usuario. " + 
            "Returns: Objeto con la información asociada.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody UsuarioEntity usuarioEntity, @PathVariable Long id) throws CiadtiException, CloneNotSupportedException{
        usuarioEntity.setId(id);
        UsuarioEntity usuarioEntityBD = usuarioService.findById(id);
        usuarioEntityBD.setPassword(usuarioEntity.getPassword() != null ? usuarioEntity.getPassword() : usuarioEntityBD.getPassword());
        usuarioEntityBD.setActivo(usuarioEntity.getActivo());
        usuarioEntityBD.setTokenPassword(usuarioEntity.getTokenPassword());
        return new ResponseEntity<>(configurationMediator.updateUser(usuarioEntityBD, usuarioEntity.getRoles() != null ? usuarioEntity.getRoles() : new ArrayList<>()), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Eliminar usuario por el id junto a su relación con los roles",
        description = "Elimina un usuario por su id junto a su relación con los roles. " + 
            "Args: id: identificador del usuario a eliminar. ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        configurationMediator.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
