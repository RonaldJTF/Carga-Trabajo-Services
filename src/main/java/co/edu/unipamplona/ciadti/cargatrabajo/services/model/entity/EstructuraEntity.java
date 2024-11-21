package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.jackson.JacksonCIADTI;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register.RegisterContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Image;
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
@Table(name = "ESTRUCTURA", schema = "FORTALECIMIENTO")
//@JsonInclude(JsonInclude.Include.ALWAYS)
public class EstructuraEntity implements Serializable, Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estr_id", nullable = false)
    private Long id;

    @Column(name = "estr_nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "estr_descripcion", length = 2000)
    private String descripcion;

    @JsonProperty("idPadre")
    @Column(name = "estr_idpadre")
    private Long idPadre;

    @Column(name = "tipo_id", nullable = false)
    private Long idTipologia;

    @JsonIgnore
    @Column(name = "estr_icono")
    private byte[] icono;

    @Column(name = "estr_mimetype")
    private String mimetype;

    @Column(name = "estr_orden")
    private Long orden;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "estr_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "estr_registradopor", nullable =  false, length = 250)
    private String registradoPor;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="estr_idpadre", insertable=false, updatable=false)
    private List<EstructuraEntity> subEstructuras;

    @JsonManagedReference
    @OneToOne(mappedBy="estructura")
    private ActividadEntity actividad;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", insertable = false, updatable = false)
    private TipologiaEntity tipologia;

    @Transient
    private String srcIcono;

    /*Almacena que tan profundo es la relación de estructura padre e hijo con este objeto como nodo raiz para una misma tipología*/
    @Transient
    private Integer subItemsDeep;

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
        this.registradoPor = registradorDTO.getJsonAsString();
    }

    public void onUpdate() {
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = registradorDTO.getJsonAsString();
    }

    @Override
    public EstructuraEntity clone() throws CloneNotSupportedException {
        EstructuraEntity cloned = (EstructuraEntity) super.clone();
        if (this.subEstructuras != null) {
            cloned.subEstructuras = new ArrayList<>();
            for (EstructuraEntity sub : this.subEstructuras) {
                cloned.subEstructuras.add(sub.clone());
            }
        }
        return cloned;
    }
}
