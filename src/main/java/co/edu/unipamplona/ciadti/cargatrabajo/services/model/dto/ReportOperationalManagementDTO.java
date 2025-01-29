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
    private Long idTipologia;
    private String tipologia;

    private String nomenclatura;
    private Double tiempoEstandar;
    private List<Double> tiemposPorNivel;
    private int elementDepth;

    private String organigrama;
    private String organigramaDescripcion;

}
