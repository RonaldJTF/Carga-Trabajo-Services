package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeStatisticDTO {
    private String nivel;
    private Double frecuencia;
    private Double tiempoMaximo;
    private Double tiempoMinimo;
    private Double tiempoUsual;
    private Double tiempoTotal;
    private Double personalTotal;
    private Double tiempoTotalGlobal;
    private Double personalTotalGlobal;
}
