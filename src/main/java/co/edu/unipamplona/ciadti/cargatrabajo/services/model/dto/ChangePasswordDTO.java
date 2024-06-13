package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordDTO {
    @NotBlank(message = "Contraseña obligatoria")
    private String password;

    @NotBlank(message = "Repetir contraseña")
    private String confirmPassword;

    @NotBlank(message = "Token obligatorio")
    private String tokenPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getTokenPassword() {
        return tokenPassword;
    }

    public void setTokenPassword(String tokenPassword) {
        this.tokenPassword = tokenPassword;
    }
}
