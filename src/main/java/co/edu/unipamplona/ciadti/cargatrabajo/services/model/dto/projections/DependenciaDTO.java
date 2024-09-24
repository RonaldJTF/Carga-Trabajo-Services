package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections;

import java.util.Date;

public interface DependenciaDTO {
    Long getId();

    String getNombre();

    String getDescripcion();

    Long getIdPadre();

    String getRegistradoPor();

    Date getFechaCambio();

    Long getIdTipo();

    byte[] getIcono();

    String getMimeType();

    Long getOrden();
}
