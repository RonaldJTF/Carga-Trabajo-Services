package co.edu.unipamplona.ciadti.cargatrabajo.services.config.security.register;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.RegistradorDTO;

public class RegisterContext {

    private static final ThreadLocal<RegistradorDTO> REGISTER = new ThreadLocal<>();

    public static RegistradorDTO getRegistradorDTO() {
        return REGISTER.get();
    }

    public static void setRegistradorDTO(RegistradorDTO registradorDTO) {
        REGISTER.set(registradorDTO);
    }
}