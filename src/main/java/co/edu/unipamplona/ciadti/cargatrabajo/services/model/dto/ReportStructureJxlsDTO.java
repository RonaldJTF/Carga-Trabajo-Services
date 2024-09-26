package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import java.util.List;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportStructureJxlsDTO implements Cloneable{
    private List<ReportStructureJxlsDTO> children;
    private ReportCellJxlsDTO cell;
    private EstructuraEntity data;
    private int index;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
