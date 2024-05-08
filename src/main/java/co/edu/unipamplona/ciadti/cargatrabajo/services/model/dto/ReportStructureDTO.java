package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportStructureDTO {
    private String dependencia;
    private String proceso;
    private String procedimiento;
    private String actividad;
    private String nivel;
    private Double frecuencia;
    private Double tiempoMinimo;
    private Double tiempoPromedio;
    private Double tiempoMaximo;
    private Double tiempoEstandar;
    private List<Double> tiemposPorNivel;

}
