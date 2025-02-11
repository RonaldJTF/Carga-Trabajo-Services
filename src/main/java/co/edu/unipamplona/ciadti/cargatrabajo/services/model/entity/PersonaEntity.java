package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

import java.io.Serializable;
import java.util.Date;

@JacksonCIADTI
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PERSONA", schema = "FORTALECIMIENTO")
public class PersonaEntity implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pers_id", nullable = false)
    private Long id;
    
    @Column(name = "tido_id", nullable = false)
    private Long idTipoDocumento;
    
    @Column(name = "gene_id", nullable = false)
    private Long idGenero;

    @Column(name = "pers_primernombre", nullable = false, length = 45)
    private String primerNombre;

    @Column(name="pers_segundonombre", length = 45)
    private String segundoNombre;

    @Column(name = "pers_primerapellido", nullable = false, length = 45)
    private String primerApellido;

    @Column(name = "pers_segundoapellido", length = 45)
    private String segundoApellido;
    
    @Column(name = "pers_numerodocumento", nullable = false, length = 45)
    private String documento;
   
    @Column(name = "pers_correo")
    private String correo;
    
    @Column(name = "pers_telefono", length = 45)
    private String telefono;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "pers_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "pers_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @JsonManagedReference
    @OneToOne (mappedBy = "persona", fetch = FetchType.EAGER)
    private FotoPersonaEntity fotoPersona;

    @JsonManagedReference
    @OneToOne(mappedBy = "persona")
    private UsuarioEntity usuario;

    @OneToOne
    @JoinColumn(name = "gene_id", insertable = false, updatable = false)
    private GeneroEntity genero;

    @OneToOne
    @JoinColumn(name = "tido_id", insertable = false, updatable = false)
    private TipoDocumentoEntity tipoDocumento;

    @Transient
    private String srcFoto;

    @JsonGetter("srcFoto")
    public String getSrcFoto() {
        return  srcFoto != null ? srcFoto :
            fotoPersona != null
            ? Image.getSrcImage(fotoPersona.getArchivo(), fotoPersona.getMimetype())
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
}
