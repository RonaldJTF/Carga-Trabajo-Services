package co.edu.unipamplona.ciadti.cargatrabajo.services.auth;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.cipher.CipherService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.email.MailService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ChangePasswordDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report.StructureReportExcel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.JwtService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PersonaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioService;
import org.thymeleaf.TemplateEngine;

import java.util.*;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;
    private final CipherService cipherService;
    private final TemplateEngine templateEngine;
    private final MailService mailService;
    private final StructureReportExcel structureReportExcel;

    @Value("${mail.urlFront}")
    private String urlFront;

    @Value("${mail.urlLogin}")
    private String urlLogin;

    public AuthenticationResponse authenticate(AuthenticationRequest authData, HttpServletRequest request) throws CiadtiException {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authData.getUsername(),
                authData.getPassword()
            )
        );

        UsuarioEntity usuario = usuarioService.findByUsername(authData.getUsername());
        usuarioService.isActivo(usuario.getId());
        PersonaEntity personaEntity = (PersonaEntity) personaService.findById(usuario.getIdPersona());
        
        Map<String, Object> extraClaims = new HashMap<>();

        var jwtToken = jwtService.generateToken(extraClaims, usuario);
        return AuthenticationResponse.builder().token(jwtToken).persona(personaEntity).build();
    }

    public void recoverPassword(PersonaEntity personaEntity) throws CiadtiException {
        UsuarioEntity usuario = usuarioService.findByUsernameOrEmail(personaEntity.getCorreo(), personaEntity.getCorreo(), "1").orElseThrow(() -> new CiadtiException("El usuario no existe", 404));

        UUID uuid = UUID.randomUUID();
        String tokenPassword = uuid.toString();

        String destinatario = usuario.getPersona().getCorreo();
        String asunto = "Recuperar contraseña - Gestión de Tiempos Laborados";

        Map<String, Object> attributesBody = new HashMap<>();
        attributesBody.put("nombre", usuario.getPersona().getPrimerNombre() + (usuario.getPersona().getSegundoNombre() != null ? " " + usuario.getPersona().getSegundoNombre() : ""));
        attributesBody.put("apellidos", usuario.getPersona().getPrimerApellido() + (usuario.getPersona().getSegundoApellido() != null ? " " + usuario.getPersona().getSegundoApellido() : ""));
        attributesBody.put("url", urlFront + tokenPassword);
        attributesBody.put("urlLogin", urlLogin);

        mailService.sendEmailForRecoverPassword(destinatario, asunto, attributesBody, null);
        usuario.setTokenPassword(tokenPassword);
        this.usuarioService.updateTokenPassword(usuario);
    }

    public void changePassword(ChangePasswordDTO data) throws CiadtiException {
        String password = cipherService.decryptCredential(data.getPassword());
        String confirmPassword = cipherService.decryptCredential(data.getConfirmPassword());
        if(!password.equals(confirmPassword)) {
            throw new CiadtiException("Las contraseñas no coinciden");
        }
        UsuarioEntity usuario = usuarioService.findByTokenPassword(data.getTokenPassword()).orElseThrow(() -> new CiadtiException("El usuario no existe", 404));
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setTokenPassword(null);
        RegistradorDTO registradorDTO = RegisterContext.getRegistradorDTO();
        registradorDTO.setUsername(usuario.getUsername());
        usuario.setRegistradoPor(registradorDTO.getJsonAsString());
        usuarioService.updatePasswordAndTokenPassword(usuario);
    }
}
