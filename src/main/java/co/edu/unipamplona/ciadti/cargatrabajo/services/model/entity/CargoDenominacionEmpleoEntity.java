package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;

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
@Table(name = "CARGODENOMINACIONEMPLEO", schema = "FORTALECIMIENTO")
public class CargoDenominacionEmpleoEntity  implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cade_id", nullable = false, length = 30)
    private Long id;

    @Column(name = "carg_id", nullable = false, length = 30)
    private Long idCargo;

    @Column(name = "deem_id", nullable = false, length = 30)
    private Long idDenominacionEmpleo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "cade_fechacambio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "cade_registradopor", nullable =  false, length = 250)
    private String registradoPor;

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
}
