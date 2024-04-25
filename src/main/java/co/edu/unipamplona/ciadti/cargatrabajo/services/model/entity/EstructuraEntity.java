package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "ESTRUCTURA", schema = "FORTALECIMIENTO")
public class EstructuraEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estr_id", nullable = false)
    private Long id;

    @Column(name = "estr_nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "estr_descripcion", length = 2000)
    private String descripcion;

    @Column(name = "estr_idpadre")
    private Long idPadre;

    @Column(name = "tipo_id", nullable = false)
    private Long idTipologia;

    @JsonIgnore
    @Column(name = "estr_icono")
    private byte[] icono;

    @Column(name = "estr_mimetype")
    private String mimetype;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "estr_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "estr_registradopor", nullable =  false, length = 250)
    private String registradoPor;

    @OneToMany
    @JoinColumn(name="estr_idpadre", insertable=false, updatable=false)
    private List<EstructuraEntity> subEstructuras;

    @JsonManagedReference
    @OneToOne(mappedBy="estructura")
    private ActividadEntity actividad;

    @OneToOne
    @JoinColumn(name = "tipo_id", insertable = false, updatable = false)
    private TipologiaEntity tipologia;

    @Transient
    private String srcIcono;

    @JsonGetter("srcIcono")
    public String getSrcFoto() {
        return  srcIcono != null ? srcIcono :
            icono != null
            ? Image.getSrcImage(icono, mimetype)
            : null;
    }
    
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
