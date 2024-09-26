package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportCellJxlsDTO implements Cloneable{
    private String value;
    private int colsToMerge;
    private int rowsToMerge;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
