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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "DEPENDENCIA", schema = "FORTALECIMIENTO")
public class DependenciaEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "depe_id", nullable = false)
    private Long id;

    @Column(name = "conv_id")
    private Long idConvencion;

    @Column(name = "depe_nombre", nullable = false, length = 1000)
    private String nombre;

    @Column(name = "depe_descripcion", length = 2000)
    private String descripcion;

    @JsonIgnore
    @Column(name = "depe_icono")
    private byte[] icono;

    @Column(name = "depe_mimetype", length = 100)
    private String mimetype;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "depe_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "depe_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @JsonIgnore
    @Transient
    private RegistradorDTO registradorDTO;
    
    @Transient
    private String srcIcono;
    
    @JsonGetter("srcIcono")
    public String getSrcFoto(){
        return  srcIcono != null ? srcIcono :
            icono != null
            ? Image.getSrcImage(icono, mimetype)
            : null;
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
}
