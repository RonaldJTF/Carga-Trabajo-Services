package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReportOperationalManagementDTO {
    private Long idGestionOperativa;
    private String proceso;
    private String procesoDescripcion;
    private String procedimiento;
    private String procedimientoDescripcion;
    private String actividad;
    private String actividadDescripcion;
    private String dependencia;
    private String dependenciaDescripcion;
    private Long idActividad;
    private Long idGestionOperativaPadre;
    private Double frecuencia;
    private Double tiempoMinimo;
    private Double tiempoMaximo;
    private Double tiempoPromedio;
    private Long idNivel;
    private String nivel;

    private String nomenclatura;
    private Double tiempoEstandar;
    private List<Double> tiemposPorNivel;
    private int elementDepth;

    public ReportOperationalManagementDTO(Long idGestionOperativa, String proceso, String procesoDescripcion, String procedimiento, String procedimientoDescripcion, String actividad, String actividadDescripcion, String dependencia, String dependenciaDescripcion, Long idActividad, Double frecuencia, Double tiempoMinimo, Double tiempoMaximo, Double tiempoPromedio, Long idNivel, String nivel, Long idGestionOperativaPadre) {
        this.idGestionOperativa = idGestionOperativa;
        this.proceso = proceso;
        this.procesoDescripcion = procesoDescripcion;
        this.procedimiento = procedimiento;
        this.procedimientoDescripcion = procedimientoDescripcion;
        this.actividad = actividad;
        this.actividadDescripcion = actividadDescripcion;
        this.dependencia = dependencia;
        this.dependenciaDescripcion = dependenciaDescripcion;
        this.idActividad = idActividad;
        this.frecuencia = frecuencia;
        this.tiempoMinimo = tiempoMinimo;
        this.tiempoMaximo = tiempoMaximo;
        this.tiempoPromedio = tiempoPromedio;
        this.idNivel = idNivel;
        this.nivel = nivel;
        this.idGestionOperativaPadre = idGestionOperativaPadre;
    }
}
