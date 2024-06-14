package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
@Data
public class RegistradorDTO {
    private String username;
    private String ip;

    public static RegistradorDTO getRegistradorDTOFromRegistradoPor(String textJson){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        return gson.fromJson(textJson, RegistradorDTO.class);
    }

    public String getJsonAsString(){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        return (gson.toJson(this));
    }
}
