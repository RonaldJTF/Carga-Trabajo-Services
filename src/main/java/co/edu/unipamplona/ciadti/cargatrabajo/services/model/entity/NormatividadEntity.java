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
@Table(name = "NORMATIVIDAD", schema = "FORTALECIMIENTO")
public class NormatividadEntity implements Serializable, Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "norm_id", nullable = false, length = 30)
    private Long id;

    @Column(name = "norm_nombre", nullable = false)
    private String nombre;

    @Column(name = "norm_descripcion", nullable = false)
    private String descripcion;

    @Column(name = "norm_emisor")
    private String emisor;

    @Column(name = "norm_fechainiciovigencia", nullable = false)
    private Date fechaInicioVigencia;
    
    @Column(name = "norm_fechafinvigencia", nullable = false)
    private Date fechaFinVigencia;

    @Column(name = "norm_estado", nullable = false, length = 1)
    private String estado;

    @Column(name = "norm_esescalasalarial", nullable = false, length = 1)
    private String esEscalaSalarial;

    @Column(name = "alca_id", nullable = false, length = 30)
    private Long idAlcance;

    @Column(name = "tino_id", nullable = false, length = 30)
    private Long idTipoNormatividad;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "norm_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "norm_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @OneToOne
    @JoinColumn(name = "alca_id", insertable = false, updatable = false)
    private AlcanceEntity alcance;

    @OneToOne
    @JoinColumn(name = "tino_id", insertable = false, updatable = false)
    private TipoNormatividadEntity tipoNormatividad;

    @JsonIgnore
    @Transient
    private RegistradorDTO registradorDTO;

    @PrePersist
    void onCreate(){
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = registradorDTO.getJsonAsString();   
    }

    public void onUpdate(){
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = registradorDTO.getJsonAsString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
