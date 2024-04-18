package co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant;

import lombok.Getter;

@Getter
public enum StaticResource {

    PATH_MAIL_IMAGE_GREET ("/Static Resources/Email/email_greet.jpeg",
            "Ruta de ubicación de la imagen de saludo que se coloca en los correos enviados."),

    PATH_MAIL_IMAGE_LOGO ("/Static Resources/Email/email_logo.png",
            "Ruta de ubicación de la imagen de logo que se coloca en los correos enviados."),

    PATH_MAIL_IMAGE_WELCOME ("/Static Resources/Email/email_welcome.png",
            "Ruta de ubicación de la imagen de bienvenida que se manda via email a los nuevos usuarios."),

    PATH_MAIL_PAGE_NEW_REQUEST ("mail/email-new-request.html",
        "Página o template de creación de nuevo solicitud. Se notifica la creación " +
        "de una solicitud por parte del registrador via email."),

    PATH_HOME_PAGE_LOGIN ("https://www.unipamplona.edu.co/",
            "Página de inicio / login del sistema.");

    private final String url;
    private final String description;

    StaticResource(String path, String description){
        this.url = path;
        this.description = description;
    }
}
