package co.edu.unipamplona.ciadti.cargatrabajo.services.auth;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ChangePasswordDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest data, HttpServletRequest request) throws CiadtiException{
        return new ResponseEntity<>(service.authenticate(data, request), HttpStatus.OK);
    }

    @PostMapping("/recover-password")
    public ResponseEntity<?> recoverPassword(@RequestBody PersonaEntity data) throws CiadtiException{
        service.recoverPassword(data);
        Map<String, String> response = new HashMap<String, String>();
        response.put("message", "Te hemos enviado un correo");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO data) throws CiadtiException{
        service.changePassword(data);
        Map<String, String> response = new HashMap<String, String>();
        response.put("message", "Contraseña actualizada");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
