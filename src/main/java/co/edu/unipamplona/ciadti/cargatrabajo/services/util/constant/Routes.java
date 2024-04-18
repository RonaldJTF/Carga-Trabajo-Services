package co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant;

import lombok.Getter;

@Getter
public enum Routes {

    PATH_ICONS ("/Icons/","Ruta de ubicación de los iconos."),
    PATH_SUPPORTS ("/Supports/","Ruta de ubicación de los archivos soportes.");

    private final String path;
    private final String description;

    Routes(String path, String description){
        this.path = path;
        this.description = description;
    }
}
