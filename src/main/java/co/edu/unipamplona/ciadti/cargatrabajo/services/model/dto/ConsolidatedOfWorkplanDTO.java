package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonGetter;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PlanTrabajoEntity;
import lombok.Builder;
import lombok.Data;

@Data
public class ConsolidatedOfWorkplanDTO {
    private PlanTrabajoEntity planTrabajo;
    private List<DateAdvance> dateAdvances;

    @Data
    @Builder
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
