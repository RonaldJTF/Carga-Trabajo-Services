package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.JacksonCIADTI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
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
@Table(name = "JERARQUIA", schema = "FORTALECIMIENTO")
public class JerarquiaEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jera_id", nullable = false)
    private Long id;

    @Column(name = "orga_id", nullable = false)
    private Long idOrganigrama;

    @Column(name = "depe_id", nullable = false)
    private Long idDependencia;

    @Column(name = "jera_idpadre")
    private Long idJerarquiaPadre;

    @Column(name = "jera_orden")
    private Long orden;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "jera_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "jera_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @OneToOne
    @JoinColumn(name = "orga_id", insertable = false, updatable = false)
    private OrganigramaEntity organigrama;

    @OneToOne
    @JoinColumn(name = "depe_id", insertable = false, updatable = false)
    private DependenciaEntity dependencia;

    @OneToMany
    @JoinColumn(name = "jera_idpadre", insertable = false, updatable = false)
    private List<JerarquiaEntity> subJerarquias;

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
}
