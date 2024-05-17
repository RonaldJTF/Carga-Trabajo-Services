package co.edu.unipamplona.ciadti.cargatrabajo.services.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.JwtService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.PersonaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.UsuarioService;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
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
        extraClaims.put("ip", getClientIpAddress(request));

        var jwtToken = jwtService.generateToken(extraClaims, usuario);
        return AuthenticationResponse.builder().token(jwtToken).persona(personaEntity).build();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
