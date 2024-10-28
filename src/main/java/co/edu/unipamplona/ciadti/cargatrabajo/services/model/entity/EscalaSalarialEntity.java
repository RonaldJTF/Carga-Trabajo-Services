package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "ESCALASALARIAL", schema = "FORTALECIMIENTO")

public class EscalaSalarialEntity implements Serializable, Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "essa_id", nullable = false, length = 30)
    private Long id;

    @Column(name = "essa_nombre", nullable = false)
    private String nombre;

    @Column(name = "essa_codigo", nullable = false, length = 100)
    private String codigo;

    @Column(name = "essa_incrementoporcentual", nullable = false, length = 30)
    private Long incrementoPorcentual;

    @Column(name = "nive_id", nullable = false, length = 30)
    private Long idNivel;

    @Column(name = "norm_id", nullable = false, length = 30)
    private Long idNormatividad;
    
    @Column(name = "essa_estado", nullable = false, length = 1)
    private String estado;

    @Column(name = "essa_fechacambio", nullable = false)
    private Date fechaCambio;

    @Column(name = "essa_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @OneToOne
    @JoinColumn(name = "norm_id", insertable = false, updatable = false)
    private NormatividadEntity normatividad;

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
