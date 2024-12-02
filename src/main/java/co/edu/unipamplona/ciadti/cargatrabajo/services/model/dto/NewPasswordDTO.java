package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewPasswordDTO {
    @NotBlank(message = "Contraseña obligatoria")
    private String password;

    @NotBlank(message = "Repetir contraseña")
    private String confirmPassword;

    @NotBlank(message = "Token obligatorio")
    private String tokenPassword;
}
