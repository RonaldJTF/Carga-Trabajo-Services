package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;

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
@Table(name = "COMPENSACIONLABNIVELVIGENCIA", schema = "FORTALECIMIENTO")
public class CompensacionLabNivelVigenciaEntity implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clnv_id", nullable = false, length = 30)
    private Long id;

    @Column(name = "nive_id", nullable = false, length = 30)
    private Long idNivel;

    @Column(name = "cola_id", nullable = false, length = 30)
    private Long idCompensacionSalarial;

    @Column(name = "essa_id", nullable = false, length = 30)
    private Long idEscalaSalarial;

    @Column(name = "vige_id", nullable = false, length = 30)
    private Long idVigencia;

    @Column(name = "regl_id", nullable = false, length = 30)
    private Long idRegla;

    @Column(name = "vari_id", nullable = false, length = 30)
    private Long idVariable;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "clnv_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "clnv_registradopor", nullable =  false, length = 250)
    private String registradoPor;

    @JoinColumn(name = "nive_id", insertable = false, updatable = false)
    private NivelEntity nivel;

    @JoinColumn(name = "cola_id", insertable = false, updatable = false)
    private CompensacionLaboralEntity compensacionSalarial;

    @JoinColumn(name = "essa_id", insertable = false, updatable = false)
    private EscalaSalarialEntity escalaSalarial;

    @JoinColumn(name = "vige_id", insertable = false, updatable = false)
    private VigenciaEntity vigencia;

    @JoinColumn(name = "regl_id", insertable = false, updatable = false)
    private ReglaEntity regla;

    @JoinColumn(name = "vari_id", insertable = false, updatable = false)
    private VariableEntity variable;

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

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
