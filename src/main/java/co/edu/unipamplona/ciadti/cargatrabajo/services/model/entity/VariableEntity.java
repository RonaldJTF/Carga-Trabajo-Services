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
@Table(name = "VARIABLE", schema = "FORTALECIMIENTO")
public class VariableEntity implements Serializable, Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vari_id", nullable = false, length = 30)
    private Long id;

    @Column(name = "vari_nombre", nullable = false)
    private String nombre;

    @Column(name = "vari_descripcion", nullable = false)
    private String descripcion;

    @Column(name = "vari_valor", nullable = false, length = 30)
    private String valor;

    @Column(name = "vari_primaria", length = 1)
    private String primaria;

    @Column(name = "vari_global", length = 1)
    private String global;

    @Column(name = "vari_porvigencia", length = 1)
    private String porVigencia;

    @Column(name = "vari_estado", length = 1)
    private String estado;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "vari_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "vari_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @Transient
    private ValorVigenciaEntity valorVigencia;

    @JsonIgnore
    @Transient
    private RegistradorDTO registradorDTO;

    @Transient
    private List<VariableEntity> variablesRelacionadas;

    @Transient
    private String expresionValor;

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
