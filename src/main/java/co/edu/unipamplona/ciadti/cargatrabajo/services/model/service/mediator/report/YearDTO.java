package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.util.List;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class YearDTO {
    private Integer value;
    private List<Month> months;
    private int numberOfDays;

    @Data
    @Builder
    public static class Month{
        private String value;
        private List<Integer> days;
    }
}

