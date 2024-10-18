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
        
        // Imágenes
        mappings.add("jpg", MediaType.IMAGE_JPEG_VALUE);
        mappings.add("jpeg", MediaType.IMAGE_JPEG_VALUE);
        mappings.add("gif", MediaType.IMAGE_GIF_VALUE);
        mappings.add("png", MediaType.IMAGE_PNG_VALUE);
        mappings.add("bmp", "image/bmp");
        mappings.add("webp", "image/webp");
        mappings.add("svg", "image/svg+xml");
        mappings.add("tiff", "image/tiff");
        mappings.add("tif", "image/tiff");
        mappings.add("ico", "image/vnd.microsoft.icon");

        // Texto
        mappings.add("txt", MediaType.TEXT_PLAIN_VALUE);
        mappings.add("html", MediaType.TEXT_HTML_VALUE);
        mappings.add("css", "text/css");
        mappings.add("js", "text/javascript");
        mappings.add("json", MediaType.APPLICATION_JSON_VALUE);
        mappings.add("xml", MediaType.APPLICATION_XML_VALUE);
        mappings.add("md", "text/markdown");
        
        // Documentos
        mappings.add("pdf", MediaType.APPLICATION_PDF_VALUE);
        mappings.add("doc", "application/msword");
        mappings.add("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mappings.add("xls", "application/vnd.ms-excel");
        mappings.add("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mappings.add("ppt", "application/vnd.ms-powerpoint");
        mappings.add("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mappings.add("odt", "application/vnd.oasis.opendocument.text");
        mappings.add("ods", "application/vnd.oasis.opendocument.spreadsheet");

        // Archivos comprimidos
        mappings.add("zip", "application/zip");
        mappings.add("gz", "application/gzip");
        mappings.add("rar", "application/x-rar-compressed");
        mappings.add("tar", "application/x-tar");
        mappings.add("7z", "application/x-7z-compressed");

        // Multimedia
        mappings.add("mp3", "audio/mpeg");
        mappings.add("wav", "audio/wav");
        mappings.add("ogg", "audio/ogg");
        mappings.add("aac", "audio/aac");
        mappings.add("mp4", "video/mp4");
        mappings.add("avi", "video/x-msvideo");
        mappings.add("mkv", "video/x-matroska");
        mappings.add("ogv", "video/ogg");
        mappings.add("webm", "video/webm");

        // Otros
        mappings.add("bin", "application/octet-stream");
        mappings.add("swf", "application/x-shockwave-flash");
        mappings.add("ttf", "application/x-font-ttf");
        mappings.add("otf", "application/x-font-opentype");
        mappings.add("woff", "application/x-font-woff");
        mappings.add("woff2", "application/x-font-woff2");
        mappings.add("config", "application/config");
        mappings.add("env", "application/x-env");
        mappings.add("jsonapi", "application/vnd.api+json");
        mappings.add("cab", "application/vnd.ms-cab-compressed");
        mappings.add("m3u8", "application/vnd.apple.mpegurl");

        return mappings;
    }

}