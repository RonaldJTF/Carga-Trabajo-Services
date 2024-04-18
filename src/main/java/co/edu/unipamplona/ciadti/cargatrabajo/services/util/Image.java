package co.edu.unipamplona.ciadti.cargatrabajo.services.util;

import java.util.Base64;

public class Image {
    public static String getHtmlImage(byte[] imageBytes, String contentType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return "<img src=\"data:"+ contentType + ";base64, " + base64Image + "\"/>";
    }

    public static String getSrcImage(byte[] imageBytes, String contentType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return "data:"+ contentType + ";base64, " + base64Image;
    }

    public static String getBase64Image(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
