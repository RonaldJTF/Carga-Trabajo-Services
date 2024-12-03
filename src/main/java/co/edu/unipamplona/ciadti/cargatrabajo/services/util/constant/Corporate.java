package co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant;

import lombok.Getter;

@Getter
public enum Corporate {
    MONTHLY_WORKING_TIME (151.3, "Tiempo laboral mensual");

    private final Double value;
    private final String description;

    Corporate(Double value, String description){
        this.value = value;
        this.description = description;
    }
}
