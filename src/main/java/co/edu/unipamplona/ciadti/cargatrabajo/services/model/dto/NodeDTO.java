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
public class NodeDTO {
    private Map<String, Object> data;
    private List<NodeDTO> children;
}
