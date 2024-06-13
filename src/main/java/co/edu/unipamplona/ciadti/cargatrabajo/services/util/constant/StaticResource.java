package co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant;

import lombok.Getter;

@Getter
public enum StaticResource {

    PATH_MAIL_IMAGE_GREET ("templates/mail/email_background_greet.png",
            "Ruta de ubicación de la imagen de background sobre la que se ubica saludo que se coloca en los correos enviados."),

    PATH_MAIL_IMAGE_LOGO ("templates/mail/email_app_logo.png",
            "Ruta de ubicación de la imagen de logo que se coloca en los correos enviados."),

    PATH_MAIL_PAGE_RECOVER_PASSWORD ("mail/email-recover-password.html",
        "Página o template de creación de nuevo solicitud. Se notifica la creación " +
        "de una solicitud por parte del registrador via email.");

    private final String url;
    private final String description;

    StaticResource(String path, String description){
        this.url = path;
        this.description = description;
    }
}
