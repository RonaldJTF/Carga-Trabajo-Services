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
@Table(name = "CARGO", schema = "FORTALECIMIENTO")

public class CargoEntity implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carg_id", nullable = false, length = 30)
    private Long id;

    @Column(name = "carg_asignacionbasica", nullable = false, length = 30)
    private Double asignacionBasicaMensual;

    @Column(name = "carg_totalcargo", nullable = false, length = 30)
    private Integer totalCargos;
  
    @Column(name = "jera_id", nullable = false, length = 30)
    private Long idJerarquia;

    @Column(name = "nive_id", nullable = false, length = 30)
    private Long idNivel;

    @Column(name = "norm_id", nullable = false, length = 30)
    private Long idNormatividad;

    @Column(name = "essa_id", nullable = false, length = 30)
    private Long idEscalaSalarial;

    @Column(name = "alca_id", nullable = false, length = 30)
    private Long idAlcance;

    @Column(name = "vige_id", nullable = false, length = 30)
    private Long idVigencia;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "carg_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "carg_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @OneToOne
    @JoinColumn(name = "jera_id", insertable = false, updatable = false)
    private JerarquiaEntity jerarquia;

    @OneToOne
    @JoinColumn(name = "nive_id", insertable = false, updatable = false)
    private NivelEntity nivel;

    @OneToOne
    @JoinColumn(name = "norm_id", insertable = false, updatable = false)
    private NormatividadEntity normatividad;

    @OneToOne
    @JoinColumn(name = "essa_id", insertable = false, updatable = false)
    private EscalaSalarialEntity escalaSalarial;

    @OneToOne
    @JoinColumn(name = "alca_id", insertable = false, updatable = false)
    private AlcanceEntity alcance;

    @OneToOne
    @JoinColumn(name = "vige_id", insertable = false, updatable = false)
    private VigenciaEntity vigencia;

    @Transient
    private List<CompensacionLabNivelVigenciaEntity> compensacionesLaboralesAplicadas;

    @Transient
    private Double asignacionTotal;

    @Transient
    private Double asignacionBasicaAnual;
    
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
