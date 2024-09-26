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
public class ReportDatasetJxlsDTO implements Cloneable{
    private List<ReportStructureJxlsDTO> structures;
    private ReportCellJxlsDTO order;
    private Activity activity; 

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Activity {
        private String level;
        private Double frecuency;
        private Double minTime;
        private Double usualTime;
        private Double maxTime;
        private List<Double> timeOfLevels;
    }
}
