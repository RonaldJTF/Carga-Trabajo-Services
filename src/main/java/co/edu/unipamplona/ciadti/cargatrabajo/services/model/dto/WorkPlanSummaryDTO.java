package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonGetter;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PlanTrabajoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkPlanSummaryDTO {
    private PlanTrabajoEntity planTrabajo;
    private List<DateAdvance> dateAdvances;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateAdvance implements Serializable{
        private Date date;
        private Double advance;
        private Double idealAdvance;
        @Builder.Default
        private String format = "dd/MM/yyyy";

        @JsonGetter("formattedDate")
        public String getFormattedDate() {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, new Locale("es", "ES"));
            String formattedDate = dateFormat.format(date);
            return formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1).toLowerCase();
        }
    }
}
