package co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@JacksonCIADTI
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USUARIO", schema = "FORTALECIMIENTO")
public class UsuarioEntity implements Serializable, Cloneable, UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usua_id", nullable = false)
    private Long id;
    
    @Column(name = "pers_id")
    private Long idPersona;
    
    @Column(name = "usua_username", nullable = false, length = 45)
    private String username;
    
    @Column(name = "usua_password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "usua_activo", nullable = false)
    private String activo;

    @Column(name = "usua_tokenpassword")
    private String tokenPassword;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "usua_fechacambio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCambio;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "usua_registradopor", nullable = false, length = 250)
    private String registradoPor;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "pers_id", insertable=false, updatable=false)
    private PersonaEntity persona;
    
    @JsonIgnore
    @Transient
    private RegistradorDTO registradorDTO;

    @PrePersist
    void onCreate() {
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = "LPR";//registradorDTO.getJsonAsString();
    }

    public void onUpdate() {
        this.registradorDTO = RegisterContext.getRegistradorDTO();
        this.fechaCambio = new Date();
        this.registradoPor = "LPR";//registradorDTO.getJsonAsString();
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        schema = "FORTALECIMIENTO",
        name = "usuariorol",
        joinColumns = {@JoinColumn(name = "usua_id", insertable = false, updatable = false)},
        inverseJoinColumns = {@JoinColumn(name = "rol_id", insertable = false, updatable = false)},
        uniqueConstraints = {@UniqueConstraint(
            columnNames = {"usua_id", "rol_id"}
        )}
    )
    private List<RolEntity> roles;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> roles = this.roles.stream().map(RolEntity :: getCodigo).toList();
        List<GrantedAuthority> authorities = roles
                .stream()
                .map(rolSeguridad -> new SimpleGrantedAuthority(rolSeguridad))
                .collect(Collectors.toList());
        return authorities;
    }
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
