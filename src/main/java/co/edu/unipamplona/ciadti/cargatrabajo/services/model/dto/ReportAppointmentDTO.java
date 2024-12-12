package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportAppointmentDTO {
    private Object data;
    private Double asignacionBasicaMensual;
    private Integer totalCargos;
    private Map<Long, Double> valueByCompensation;
    private List<ComparativeAttribute> comparativesByScope;
    private List<ReportAppointmentDTO> children;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComparativeAttribute {
        private String key;
        private String name;
        private Integer totalCargos;
        private Double asignacionBasicaAnual;
    }
}