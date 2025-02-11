package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.JacksonCIADTI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JacksonCIADTI
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ACTIVIDADGESTION", schema = "FORTALECIMIENTO")
public class ActividadGestionEntity implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acge_id", nullable = false)
    private Long id;

    @Column(name = "nive_id", nullable = false)
    private Long idNivel;

    @Column(name = "geop_id", nullable = false)
    private Long idGestionOperativa;

    @Column(name = "acge_frecuencia", nullable = false)
    private Double frecuencia;

    @Column(name = "acge_tiempomaximo", nullable = false)
    private Double tiempoMaximo;

    @Column(name = "acge_tiempominimo", nullable = false)
    private Double tiempoMinimo;

    @Column(name = "acge_tiempopromedio")
    private Double tiempoPromedio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "acge_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "acge_registradopor", nullable =  false, length = 250)
    private String registradoPor;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "geop_id", insertable = false, updatable = false)
    private GestionOperativaEntity gestionOperativa;

    @OneToOne
    @JoinColumn(name = "nive_id", insertable = false, updatable = false)
    private NivelEntity nivel;

    @JsonIgnore
    @Transient
    private RegistradorDTO registradorDTO;

    @Transient
    private List<Double> timePerLevel;

    @Transient
    private Double tiempoEstandar;

    @JsonGetter("tiempoMinimoEnHoras")
    public Double getTiempoMinimoEnHoras(){
        return (double) Math.round((tiempoMinimo / 60.0)*100)/100;
    }

    @JsonGetter("tiempoMaximoEnHoras")
    public Double getTiempoMaximoEnHoras(){
        return (double) Math.round((tiempoMaximo / 60.0)*100)/100;
    }

    @JsonGetter("tiempoPromedioEnHoras")
    public Double getTiempoPromedioEnHoras(){
        return (double) Math.round((tiempoPromedio / 60.0)*100)/100;
    }

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

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
