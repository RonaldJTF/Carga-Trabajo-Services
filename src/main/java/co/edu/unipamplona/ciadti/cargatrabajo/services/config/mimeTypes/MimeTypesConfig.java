package co.edu.unipamplona.ciadti.cargatrabajo.services.config.mimeTypes;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
public class MimeTypesConfig {
    @Bean
    public static MimeMappings mimeMappings() {
        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
        mappings.add("jpg", MediaType.IMAGE_JPEG_VALUE);
        mappings.add("jpeg", MediaType.IMAGE_JPEG_VALUE);
        mappings.add("gif", MediaType.IMAGE_GIF_VALUE);
        mappings.add("png", MediaType.IMAGE_PNG_VALUE);
        mappings.add("txt", MediaType.TEXT_PLAIN_VALUE);
        mappings.add("pdf", MediaType.APPLICATION_PDF_VALUE);
        mappings.add("html", MediaType.TEXT_HTML_VALUE);
        mappings.add("css", "text/css");
        mappings.add("js", "text/javascript");
        mappings.add("bmp", "image/bmp");
        mappings.add("webp", "image/webp");
        mappings.add("svg", "image/svg+xml");
        mappings.add("ico", "image/x-icon");
        return mappings;
    }
}