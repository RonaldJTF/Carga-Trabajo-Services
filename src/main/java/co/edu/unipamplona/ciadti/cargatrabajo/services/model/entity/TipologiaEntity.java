package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.JacksonCIADTI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "TIPOLOGIA", schema = "FORTALECIMIENTO")
public class TipologiaEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_id", nullable = false)
    private Long id;

    @Column(name = "tipo_idtipologiasiguiente")
    private Long idTipologiaSiguiente;

    @Column(name = "tipo_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_claseicono", length = 50)
    private String claseIcono;

    @Column(name = "tipo_nombrecolor", length = 50)
    private String nombreColor;

    @Column(name = "tipo_esdependencia", length = -1)
    private String esDependencia;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "tipo_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "tipo_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        schema = "FORTALECIMIENTO",
        name = "TIPOLOGIAACCION",
        joinColumns = {@JoinColumn(name = "tipo_id", insertable = false, updatable = false)},
        inverseJoinColumns = {@JoinColumn(name = "acci_id", insertable = false, updatable = false)},
        uniqueConstraints = {@UniqueConstraint(
            columnNames = {"tipo_id", "acci_id"}
        )}
    )
    private List<AccionEntity> acciones;

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
