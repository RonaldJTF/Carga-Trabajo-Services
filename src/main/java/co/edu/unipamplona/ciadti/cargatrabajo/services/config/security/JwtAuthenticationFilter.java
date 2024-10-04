package co.edu.unipamplona.ciadti.cargatrabajo.services.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Metadata;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @SuppressWarnings("null") @NotNull HttpServletRequest request,
            @SuppressWarnings("null") @NotNull HttpServletResponse response,
            @SuppressWarnings("null") @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(Metadata.AUTHORIZATION);
        final String jwt;

        if (authHeader == null || !authHeader.startsWith(Metadata.BEARER + " ")) {
            RegistradorDTO registradorDTO = new RegistradorDTO();
            registradorDTO.setIp(getClientIpAddress(request));
            RegisterContext.setRegistradorDTO(registradorDTO);
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        this.procesar(request, jwt);
        filterChain.doFilter(request, response);
    }

    private void procesar(HttpServletRequest request, String jwt){
        final String username;
        username = jwtService.extractUsername(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                //Sección aprovechada para obtener toda información de quien ejecuta la acción a través del token
                String ip = getClientIpAddress(request);
                RegistradorDTO registradorDTO = jwtService.extractRegistradorDTO(jwt);
                registradorDTO.setIp(ip);
                RegisterContext.setRegistradorDTO(registradorDTO);
            }
        }
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
