package co.edu.unipamplona.ciadti.cargatrabajo.services.config.ftp;

public class FtpContext {
    private static final ThreadLocal<String> FTP = new ThreadLocal<>();

    public static String getFTP() {
        return FTP.get();
    }

    public static void setFTP(String ftp) {
        FTP.set(ftp);
    }
}
