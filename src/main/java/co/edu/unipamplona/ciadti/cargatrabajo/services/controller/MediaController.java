package co.edu.unipamplona.ciadti.cargatrabajo.services.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.ftp.FtpContext;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.ftp.FtpManagerService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ArchivoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ArchivoService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final FtpManagerService ftpManagerService;
    private final ArchivoService archivoService;

    @Operation(
        summary = "Cargar archivo a un servidor FTP",
        description = "Carga un archivo a un servidor FTP. " +
                "Args: multipartFile: objeto MultipartFile. " +
                "Returns: Estado de la operación. " +
                "Nota: Se debe establecer el header X-ftp con el nombre del servidor FTP, de lo contrario se " +
                "tomará el ftp configurado por defecto.")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("multipartFile") MultipartFile multipartFile, HttpServletRequest request) {
        FtpContext.setFTP(request.getHeader("X-ftp"));
        FileOutputStream fos = null;
        FileInputStream fileInputStream = null;
        try {
            File file = new File(multipartFile.getOriginalFilename());
            fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fileInputStream = new FileInputStream(file);
            String path = String.format("/" + multipartFile.getOriginalFilename());
            ftpManagerService.uploadFile(fileInputStream, path);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
           return Methods.handleRapException(new CiadtiException(e.getMessage(), 400));
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
    }


    @Operation(
            summary = "Descargar archivo de un servidor FTP por el id del archivo",
            description = "Descarga un archivo de un servidor FTP. " +
                    "Args: idArchivo: id del archivo. " +
                    "Returns: Archivo descargado.")
    @GetMapping("/download/{idArchivo}")
    public ResponseEntity<?> downloadFile(@PathVariable Long idArchivo) throws CiadtiException {
        ArchivoEntity  archivoEntity = (ArchivoEntity) archivoService.findById(idArchivo);

        FtpContext.setFTP(archivoEntity.getFtp().getCodigo());
        try {
            byte[]  fileBytes = ftpManagerService.downloadFile(archivoEntity.getPath());
            if (fileBytes != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + archivoEntity.getNombre());
                headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition, Content-Type");
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.parseMediaType(archivoEntity.getMimetype()))
                        .body(fileBytes);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }  catch (Exception e) {
            return Methods.handleRapException(new CiadtiException(e.getMessage(), 400));
        }
    }
}
