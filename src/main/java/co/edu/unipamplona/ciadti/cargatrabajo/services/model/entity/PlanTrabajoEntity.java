package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.JacksonCIADTI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
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
@Table(name = "PLANTRABAJO", schema = "FORTALECIMIENTO")
public class PlanTrabajoEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pltr_id", nullable = false)
    private Long id;

    @Column(name = "pltr_nombre", nullable = false)
    private String nombre;

    @Column(name = "pltr_descripcion")
    private String descripcion;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "pltr_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "pltr_registradopor", nullable = false, length = 250)
    private String registradoPor; 

    @JsonManagedReference
    @OneToMany(mappedBy="planTrabajo")
    private List<EtapaEntity> etapas;

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
