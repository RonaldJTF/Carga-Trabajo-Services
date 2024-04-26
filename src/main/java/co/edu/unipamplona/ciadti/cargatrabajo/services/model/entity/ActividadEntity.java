package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.OneToOne;
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
@Table(name = "ACTIVIDAD", schema = "FORTALECIMIENTO")
public class ActividadEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acti_id", nullable = false)
    private Long id;

    @Column(name = "nive_id", nullable = false)
    private Long idNivel;

    @Column(name="estr_id", nullable = false)
    private Long idEstructura;

    @Column(name = "acti_frecuencia", nullable = false)
    private Double frecuencia;

    @Column(name = "acti_tiempomaximo", nullable = false)
    private Double tiempoMaximo;

    @Column(name = "acti_tiempominimo", nullable = false)
    private Double tiempoMinimo;

    @Column(name = "acti_tiempopromedio")
    private Double tiempoPromedio;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "acti_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "acti_registradopor", nullable =  false, length = 250)
    private String registradoPor;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name="estr_id", insertable = false, updatable = false)
    private EstructuraEntity estructura;

    @OneToOne
    @JoinColumn(name = "nive_id", insertable = false, updatable = false)
    private NivelEntity nivel;

    @JsonIgnore
    @Transient
    private RegistradorDTO registradorDTO;

    @PrePersist
    void onCreate() {
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = "registradorDTO.getJsonAsString()";
    }

    public void onUpdate() {
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = "registradorDTO.getJsonAsString()";
    }
}
