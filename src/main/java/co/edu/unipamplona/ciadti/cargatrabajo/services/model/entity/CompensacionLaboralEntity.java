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
@Table(name = "COMPENSACIONLABORAL", schema = "FORTALECIMIENTO")

public class CompensacionLaboralEntity implements Serializable, Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cola_id", nullable = false, length = 30)
    private Long id;

    @Column(name = "cola_nombre", nullable = false)
    private String nombre;

    @Column(name = "cola_descripcion", nullable = false)
    private String descripcion;

    @Column(name = "cola_estado", nullable = false, length = 1)
    private String estado;

    @Column(name = "cate_id", nullable = false, length = 30)
    private Long idCategoria;
    
    @Column(name = "peri_id", nullable = false, length = 30)
    private Long idPeriodicidad;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "cola_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "cola_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @JoinColumn(name = "cate_id", insertable = false, updatable = false)
    private CategoriaEntity categoria;
    
    @JoinColumn(name = "peri_id", insertable = false, updatable = false)
    private PeriodicidadEntity periodicidad;

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
