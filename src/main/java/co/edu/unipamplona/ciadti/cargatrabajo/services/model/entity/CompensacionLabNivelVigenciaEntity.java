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
@Table(name = "COMPENSACIONLABNIVELVIGENCIA", schema = "FORTALECIMIENTO")
public class CompensacionLabNivelVigenciaEntity implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clnv_id", nullable = false, length = 30)
    private Long id;

    @Column(name = "nive_id", nullable = false, length = 30)
    private Long idNivel;

    @Column(name = "cola_id", nullable = false, length = 30)
    private Long idCompensacionLaboral;

    @Column(name = "essa_id", nullable = false, length = 30)
    private Long idEscalaSalarial;

    @Column(name = "vige_id", nullable = false, length = 30)
    private Long idVigencia;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "clnv_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "clnv_registradopor", nullable =  false, length = 250)
    private String registradoPor;

    @Transient
    private NivelEntity nivel;

    @Transient
    private CompensacionLaboralEntity compensacionLaboral;

    @Transient
    private EscalaSalarialEntity escalaSalarial;

    @Transient
    private VigenciaEntity vigencia;

    @Transient
    private List<CompensacionLabNivelVigValorEntity> valoresCompensacionLabNivelVigencia;

    @Transient
    private Double valorAplicado;

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
