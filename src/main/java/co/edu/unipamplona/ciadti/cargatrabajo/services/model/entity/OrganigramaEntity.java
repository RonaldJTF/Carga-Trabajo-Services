package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.JacksonCIADTI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JacksonCIADTI
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ORGANIGRAMA", schema = "FORTALECIMIENTO")
public class OrganigramaEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orga_id", nullable = false)
    private Long id;

    @Column(name = "orga_nombre", nullable = false, length = 1000)
    private String nombre;

    @Column(name = "orga_descripcion", length = 2000)
    private String descripcion;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "norm_id")
    private Long idNormatividad;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "orga_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "orga_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @OneToOne
    @JoinColumn(name = "norm_id", insertable = false, updatable = false)
    private NormatividadEntity normatividad;

    @JsonIgnore
    @Transient
    private RegistradorDTO registradorDTO;

    @PrePersist
    void onCreate() {
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = registradorDTO.getJsonAsString();
    }

    public void onUpdate() {
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = registradorDTO.getJsonAsString();
    }

    @JsonSetter("idNormatividad")
    public void setIdNormatividad(Long idNormatividad) {
        if (idNormatividad != null && idNormatividad > 0) {
            this.idNormatividad = idNormatividad;
        } else {
            this.idNormatividad = null;
        }
    }

    @JsonSetter("normatividad")
    public void setNormatividad(NormatividadEntity normatividad) {
        if (normatividad != null && normatividad.getId() != null) {
            this.normatividad = normatividad;
        } else {
            this.normatividad = null;
        }
    }
}
