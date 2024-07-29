package co.edu.unipamplona.ciadti.cargatrabajo.services.config.ftp;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Realiza la configuración de los servidores FTPs
 * Nota: El nombre de las variables se definen en el respectivo properties del profile activo, por lo que son leídas, y luego
 * con ese nombre se trae su valor de las variables de entorno del sistema.
 * */
@Configuration
public class FtpConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    /*@Bean
    public Map<String, FTPClient> ftpClient() throws IOException {
        File[] files = this.getAllFtpFiles();
        Map<String, FTPClient> resolvedFtps= new HashMap<>();
        for (File propertyFile : files) {
            Properties ftpProperties = new Properties();
            FTPClient ftpClient = new FTPClient();
            try {
                ftpProperties.load(new FileInputStream(propertyFile));
                String ftpId = ftpProperties.getProperty("name");

                ftpClient.connect(ftpProperties.getProperty("ftp.server"), Integer.parseInt(ftpProperties.getProperty("ftp.port")));
                ftpClient.login(ftpProperties.getProperty("ftp.username"), ftpProperties.getProperty("ftp.password"));
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.addProtocolCommandListener(new ProtocolCommandListener() {
                    @Override
                    public void protocolCommandSent(ProtocolCommandEvent protocolCommandEvent) {
                        System.out.printf("[%s][%d] Command sent : [%s]-%s", Thread.currentThread().getName(),
                                System.currentTimeMillis(), protocolCommandEvent.getCommand(),
                                protocolCommandEvent.getMessage());
                    }
                    @Override
                    public void protocolReplyReceived(ProtocolCommandEvent protocolCommandEvent) {
                        System.out.printf("[%s][%d] Reply received : %s", Thread.currentThread().getName(),
                                System.currentTimeMillis(), protocolCommandEvent.getMessage());
                    }
                });
                resolvedFtps.put(ftpId, ftpClient);
            } catch (IOException exp) {
                throw new RuntimeException("Problem in ftp datasource:" + exp);
            }
        }
        return resolvedFtps;
    }*/

    private File[] getAllFtpFiles() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + activeProfile + "/allFTPs");
        File tenantDirectory = resource.getFile();
        return tenantDirectory.listFiles();
    }
}

