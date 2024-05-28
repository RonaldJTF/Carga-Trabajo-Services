package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.persistence.ManyToOne;
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
@Table(name = "SEGUIMIENTO", schema = "FORTALECIMIENTO")
public class SeguimientoEntity implements Serializable, Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "segu_id", nullable = false)
    private Long id;

    @Column(name = "tare_id", nullable = false)
    private Long idTarea;

    @Column(name = "segu_porcentajeavance", nullable = false)
    private Double porcentajeAvance;

    @Column(name = "segu_observacion")
    private String observacion;

    @Column(name = "segu_activo")
    private String activo;

    @Column(name = "segu_fecha", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "segu_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "segu_registradopor", nullable = false, length = 250)
    private String registradoPor; 

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        schema = "FORTALECIMIENTO",
        name = "seguimientoarchivo",
        joinColumns = {@JoinColumn(name = "segu_id", insertable = false, updatable = false)},
        inverseJoinColumns = {@JoinColumn(name = "arch_id", insertable = false, updatable = false)},
        uniqueConstraints = {@UniqueConstraint(
            columnNames = {"segu_id", "arch_id"}
        )}
    )
    private List<ArchivoEntity> archivos;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "tare_id", insertable = false, updatable = false)
    private TareaEntity tarea;

    @JsonIgnore
    @Transient
    private RegistradorDTO registradorDTO;

    @PrePersist
    void onCreate() {
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = registradorDTO.getJsonAsString();
        this.fecha = new Date();
    }

    public void onUpdate() {
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = registradorDTO.getJsonAsString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
