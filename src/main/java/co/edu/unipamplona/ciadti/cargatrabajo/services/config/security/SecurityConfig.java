package co.edu.unipamplona.ciadti.cargatrabajo.services.config.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.tenant.TenantFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final TenantFilter tenantFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/doc/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/statistics/**", "/api/structure/report").hasAnyRole("SUPERADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole( "SUPERADMINISTRADOR", "ADMINISTRADOR", "OPERADOR")
                
                .requestMatchers(HttpMethod.GET, "/api/structure/**").hasAnyRole( "SUPERADMINISTRADOR", "ADMINISTRADOR", "OPERADOR")
                .requestMatchers("/api/structure/**").hasAnyRole( "ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/api/workplan/**").hasAnyRole( "SUPERADMINISTRADOR", "ADMINISTRADOR", "OPERADOR")
                .requestMatchers("/api/workplan/**").hasAnyRole("ADMINISTRADOR")
                
                .requestMatchers("/api/user/validate-password", "/api/user/new-password").authenticated()
                .requestMatchers("/api/user/**").hasRole("SUPERADMINISTRADOR")

                .requestMatchers(HttpMethod.PUT,"/api/person/**").authenticated()
                .requestMatchers(HttpMethod.GET,"/api/person/**").authenticated()
                .requestMatchers("/api/person/**").hasRole("SUPERADMINISTRADOR")

                .requestMatchers("/api/media/**").authenticated()
                
                .requestMatchers(HttpMethod.GET, "/api/position/**","/api/periodicity/**","/api/category/**","/api/scope/**", "/api/document-type/**", "/api/gender/**", "/api/level/**", "/api/role/**", "/api/typology/**", "/api/ftp/**", "/api/action/**", "/api/typology-action/**").authenticated()
                .requestMatchers("/api/position/**","/api/periodicity/**","/api/category/**","/api/scope/**","/api/document-type/**", "/api/gender/**", "/api/level/**", "/api/role/**", "/api/typology/**", "/api/ftp/**", "/api/action/**", "/api/typology-action/**").hasAnyRole("DESARROLLADOR")
                
                .anyRequest().denyAll()
            )
            .cors((cors) -> cors.configurationSource(corsConfigurationSource))
			.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
				.frameOptions(frameOptions -> frameOptions.disable())
			)
            .authenticationProvider(authenticationProvider)
            .addFilterAfter(tenantFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtAuthFilter, TenantFilter.class)
			.csrf(csrf -> csrf.disable())
            .build();
    }
}
