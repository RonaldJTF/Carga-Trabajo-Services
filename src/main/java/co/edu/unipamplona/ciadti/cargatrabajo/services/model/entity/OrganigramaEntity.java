package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.JacksonCIADTI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Image;

import com.fasterxml.jackson.annotation.JsonSetter;
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
@Table(name = "ORGANIGRAMA", schema = "FORTALECIMIENTO")
public class OrganigramaEntity implements Serializable, Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orga_id", nullable = false)
    private Long id;

    @Column(name = "orga_nombre", nullable = false, length = 1000)
    private String nombre;

    @Column(name = "orga_descripcion", length = 2000)
    private String descripcion;

    @Column(name = "norm_id")
    private Long idNormatividad;

    @JsonIgnore
    @Column(name = "orga_diagrama")
    private byte[] diagrama;

    @Column(name = "orga_mimetype", length = 100)
    private String mimetype;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "orga_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "orga_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @OneToOne
    @JoinColumn(name = "norm_id", insertable = false, updatable = false)
    private NormatividadEntity normatividad;

    @Transient
    private String srcDiagrama;
    
    @JsonGetter("srcDiagrama")
    public String getSrcImage(){
        return  srcDiagrama != null ? srcDiagrama :
            diagrama != null
            ? Image.getSrcImage(diagrama, mimetype)
            : null;
    }

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
    
    @JsonSetter("idNormatividad")
    public void setIdNormatividad(Long idNormatividad) {
        if (idNormatividad != null && idNormatividad > 0) {
            this.idNormatividad = idNormatividad;
        } else {
            this.idNormatividad = null;
        }
    }

    @JsonSetter("normatividad")
    public void setNormatividad(NormatividadEntity normatividad) {
        if (normatividad != null && normatividad.getId() != null) {
            this.normatividad = normatividad;
        } else {
            this.normatividad = null;
        }
    }
}
