package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import lombok.Data;

@Data
public class ActividadOutDTO {
    private String nombre;
    private String nivel;
    private String extrae;
    private Double frecuencia;
    private Double tiempoMaximo;
    private Double tiempoMinimo;
    private Double tiempoUsual;
    private Double tiempoEstandar;
    private Double tiempoTotalTarea;

    public ActividadOutDTO() {
    }

    public ActividadOutDTO(String nombre, String nivel, String extrae, Double frecuencia, Double tiempoMaximo, Double tiempoMinimo, Double tiempoUsual, Double tiempoEstandar, Double tiempoTotalTarea) {
        this.nombre = nombre;
        this.nivel = nivel;
        this.extrae = extrae;
        this.frecuencia = frecuencia;
        this.tiempoMaximo = tiempoMaximo;
        this.tiempoMinimo = tiempoMinimo;
        this.tiempoUsual = tiempoUsual;
        this.tiempoEstandar = tiempoEstandar;
        this.tiempoTotalTarea = tiempoTotalTarea;
    }

}
