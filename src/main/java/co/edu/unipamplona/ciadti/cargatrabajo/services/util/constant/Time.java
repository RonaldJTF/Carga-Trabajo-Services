package co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant;

import lombok.Getter;

/**
 * El atributo 'value' es el tiempo en milisegundos para cada uno de los tipos de tiempo.
 */
@Getter
public enum Time {
    YEAR ( 31536000000L),
    MONTH ( 2592000000L),
    WEEK ( 604800000L),
    DAY ( 86400000L),
    HOUR ( 3600000L),
    MINUTE ( 60000L),
    SECOND ( 1000L),
    MILLISECONDS ( 1L);

    private final Long value;

    Time(Long value){
        this.value = value;
    }

}
