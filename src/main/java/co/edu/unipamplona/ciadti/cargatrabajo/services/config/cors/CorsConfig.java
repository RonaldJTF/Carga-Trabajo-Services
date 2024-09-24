package co.edu.unipamplona.ciadti.cargatrabajo.services.config.cors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://172.26.123.123:9500", "https://proyecto.unipamplona.edu.co"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                Arrays.asList(
                        "Origin",
                        "Accept",
                        "Enctype",
                        "X-Requested-With",
                        "X-Tenant-Id",
                        "Content-Type",
                        "Content-Disposition",
                        "Access-Control-Request-Method",
                        "Access-Control-Request-Headers",
                        "Authorization",
                        "sentry-trace", // Genera un identificador único para cada solicitud que se haga. Permite que Sentry pueda rastrear la solicitud por diferentes servicios.
                        "baggage"       // Permite agregar información adicional a la solicitud, por ejemplo, información del usuario o información de contexto, por defecto se accede a la configuración definida arriba.
                ));
        configuration.setExposedHeaders(
                Arrays.asList(
                        "Referer",
                        "Content-Type",
                        "Content-Disposition",
                        "Access-Control-Allow-Origin",
                        "Access-Control-Allow-Credentials"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

