package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.ftp.FtpContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.ftp.FtpManagerService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ArchivoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.FtpEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ArchivoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.FtpService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
@Service
public class MediaMediator {
    private final FtpManagerService ftpManagerService;
    private final ArchivoService archivoService;
    private final FtpService ftpService;

    public ArchivoEntity saveFile(MultipartFile multipartFile, String path) {
        ArchivoEntity archivoEntity;
        FileOutputStream fos = null;
        FileInputStream fileInputStream = null;
        try {
            FtpEntity ftpEntityBD = ftpService.findActive();
            FtpContext.setFTP(ftpEntityBD.getCodigo());

            String originalFilename = multipartFile.getOriginalFilename();
            byte[] bytes = multipartFile.getBytes();
            long size = multipartFile.getSize();
            String contentType = multipartFile.getContentType();
            String name = multipartFile.getName();

            path = StringUtils.prependIfMissing(path, "/") + Methods.generateUniqueValue(originalFilename);

            File file = new File(originalFilename);
            fos = new FileOutputStream(file);
            fos.write(bytes);
            fileInputStream = new FileInputStream(file);

            ftpManagerService.uploadFile(fileInputStream, path);

            archivoEntity = ArchivoEntity.builder()
                    .nombre(originalFilename)
                    .path(path)
                    .idFtp(ftpEntityBD.getId())
                    .tamanio(size)
                    .mimetype(contentType)
                    .bytes(bytes).build();
            archivoEntity = (ArchivoEntity) archivoService.save(archivoEntity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return archivoEntity;
    }
}
