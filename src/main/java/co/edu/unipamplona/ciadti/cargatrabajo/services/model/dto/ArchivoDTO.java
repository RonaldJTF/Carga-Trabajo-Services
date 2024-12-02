package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchivoDTO {
    private String filename;
    private byte[] fileBytes;
    private String extension;
    private String path;

    public String getFilename(){
        if (filename == null) {
            if (path != null) {
                String[] obj = path.split("/");
                if (obj.length > 1) {
                    filename = obj[obj.length - 1];
                }
            }
        }
        return  filename;
    }
}
