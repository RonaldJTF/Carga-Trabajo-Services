package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Node {
    private String nombre;
    private String descripcion;
    private List<Node> subEstructuras;
    private Integer id;

    public Node(String nombre, Integer id) {
        this.nombre = nombre;
        this.id = id;
        this.subEstructuras = new ArrayList<>();
    }

    public Node(String nombre, Integer id, String description) {
        this.nombre = nombre;
        this.id = id;
        this.descripcion = description;
        this.subEstructuras = new ArrayList<>();
    }

    public void addChild(Node child) {
        this.subEstructuras.add(child);
    }
}
