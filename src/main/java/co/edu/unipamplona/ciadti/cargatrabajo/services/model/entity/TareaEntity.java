package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.JacksonCIADTI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.deserializer.DateDeserializer;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "TAREA", schema = "FORTALECIMIENTO")
public class TareaEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tare_id", nullable = false)
    private Long id;

    @Column(name = "tare_nombre", nullable = false)
    private String nombre;

    @Column(name = "tare_descripcion")
    private String descripcion;

    @Column(name = "etap_id", nullable = false)
    private Long idEtapa;
    
    @JsonDeserialize(using = DateDeserializer.class)
    @Column(name = "tare_fechainicio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio;

    @JsonDeserialize(using = DateDeserializer.class)
    @Column(name = "tare_fechafin", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFin;

    @Column(name = "tare_entregable", nullable = false)
    private String entregable;

    @Column(name = "tare_responsable", nullable = false)
    private String responsable;

    @Column(name = "tare_activo")
    private String activo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "tare_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "tare_registradopor", nullable = false, length = 250)
    private String registradoPor; 

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "etap_id", insertable = false, updatable = false)
    private EtapaEntity etapa;

    @JsonManagedReference
    @OneToMany(mappedBy = "tarea")
    private List<SeguimientoEntity> seguimientos;
    
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
