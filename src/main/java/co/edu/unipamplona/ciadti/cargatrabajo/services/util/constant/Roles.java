package co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant;

import lombok.Getter;

@Getter
public enum Roles {
    ROLE_ADMINISTRATOR ("ROLE_ADMIN", "Rol de administrador."),
    ROLE_PROFESSOR ("ROLE_DOCENTE", "Rol de docente."),
    ROLE_EVALUATOR( "ROLE_EVALUADOR", "Rol de evaluado");

    private final String code;
    private final String description;

    Roles(String code, String description){
    this.code = code;
    this.description = description;
}
}
