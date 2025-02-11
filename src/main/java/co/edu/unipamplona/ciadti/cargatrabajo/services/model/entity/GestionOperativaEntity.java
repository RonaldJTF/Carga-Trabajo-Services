package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
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
@Table(name="GESTIONOPERATIVA", schema = "FORTALECIMIENTO")
public class GestionOperativaEntity implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "geop_id", nullable=false)
    private Long id;

    @Column(name = "geop_idpadre")
    private Long idPadre;

    @Column(name = "tipo_id", nullable = false)
    private Long idTipologia;

    @Column(name = "geop_nombre", nullable = false, length = 1000)
    private String nombre;

    @Column(name = "geop_descripcion", length = 2000)
    private String descripcion;

    @Column(name = "geop_orden")
    private Long orden;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "geop_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "geop_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @OneToOne
    @JoinColumn(name = "tipo_id", insertable = false, updatable = false)
    private TipologiaEntity tipologia;

    @OneToMany
    @JoinColumn(name="geop_idpadre", insertable=false, updatable=false)
    private List<GestionOperativaEntity> subGestionesOperativas;

    @JsonManagedReference
    @OneToOne(mappedBy="gestionOperativa")
    private ActividadGestionEntity actividad;

    @JsonIgnore
    @Transient
    private RegistradorDTO registradorDTO;

    @Transient
    private Long idJerarquiaGestionOperativa;

    @Transient
    private DependenciaEntity dependencia;

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
    public GestionOperativaEntity clone() throws CloneNotSupportedException {
        GestionOperativaEntity cloned = (GestionOperativaEntity) super.clone();
        if (this.subGestionesOperativas != null) {
            cloned.subGestionesOperativas = new ArrayList<>();
            for (GestionOperativaEntity sub : this.subGestionesOperativas) {
                cloned.subGestionesOperativas.add(sub.clone());
            }
        }
        return cloned;
    }
}
