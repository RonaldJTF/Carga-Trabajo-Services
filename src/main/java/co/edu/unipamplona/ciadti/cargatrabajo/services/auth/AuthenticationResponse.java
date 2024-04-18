package co.edu.unipamplona.ciadti.cargatrabajo.services.auth;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PersonaEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private PersonaEntity persona;
}
