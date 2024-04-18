package co.edu.unipamplona.ciadti.cargatrabajo.services.config.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.JwtService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Metadata;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter  extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    /**
     * Este filtro obtiene el tenant_id correspondiente a la instancia de Base de datos.
     * El valor puede ser tomado desde el token, o desde el parámetro en cabecera X-TenantID, dando siempre prioridad a este último.
     * Si el token ni el X-TenantID no están definidos, entonces la variable tenantName es nula, lo que
     * permite luego definir la instancia con tenant por default.
    * */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tenantName = request.getHeader(Metadata.X_TENANT_ID);
        if (tenantName == null){
            final String authHeader = request.getHeader(Metadata.AUTHORIZATION);
            final String jwt;
            if (authHeader != null && authHeader.startsWith(Metadata.BEARER + " ")) {
                jwt = authHeader.substring(7);
                tenantName = jwtService.extractTenantId(jwt);
            }
        }
        TenantContext.setCurrentTenant(tenantName);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.setCurrentTenant("");
        }
    }
}