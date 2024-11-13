package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupAttribute {
    private String groupKey;
    private String groupValue;
    private String type;
}
