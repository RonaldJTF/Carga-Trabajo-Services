package co.edu.unipamplona.ciadti.cargatrabajo.services.config.email;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.StaticResourceMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.StaticResource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ArchivoDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${mail.sender}")
    private String sender;

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final StaticResourceMediator staticResourceMediator;

    /**
     * Envía un email básico con un arreglo de archivos anexos o sin ningún archivo anexo.
     * */
    @Async
    public CompletableFuture<Boolean> sendMail(String destinatario, String asunto, String mensaje, ArrayList<ArchivoDTO> listaArchivos) {
        try {
            MimeMessage correo = mailSender.createMimeMessage();
            MimeMessageHelper helper = buildCommonMimeMessageHelper(correo, destinatario, asunto, listaArchivos);
            helper.setText(mensaje, true);
            mailSender.send(correo);
        }  catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo electrónico", e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(true);
    }

    @Async
    public CompletableFuture<Boolean> sendEmailForRecoverPassword(String destinatario, String asunto, Map<String, Object> attributesBody, ArrayList<ArchivoDTO> listaArchivos) {
        try {
            MimeMessage correo = mailSender.createMimeMessage();
            MimeMessageHelper helper = buildCommonMimeMessageHelper(correo, destinatario, asunto, listaArchivos);

            ArchivoDTO imageLogo = new ArchivoDTO();
            ArchivoDTO imageGreet = new ArchivoDTO();

            imageLogo.setPath(StaticResource.PATH_MAIL_IMAGE_LOGO.getUrl());
            imageLogo.setFileBytes(staticResourceMediator.getResourceBytes(StaticResource.PATH_MAIL_IMAGE_LOGO.getUrl()));
            String contentTypeLogo = Methods.getContentType(imageLogo.getFilename()).toString();

            imageGreet.setPath(StaticResource.PATH_MAIL_IMAGE_GREET.getUrl());
            imageGreet.setFileBytes(staticResourceMediator.getResourceBytes(StaticResource.PATH_MAIL_IMAGE_GREET.getUrl()));
            String contentTypeGreet = Methods.getContentType(imageGreet.getFilename()).toString();

            attributesBody.put("imageLogo", imageLogo.getFilename());
            attributesBody.put("imageGreet", imageGreet.getFilename());

            String mensaje = templateEngine.process(StaticResource.PATH_MAIL_PAGE_RECOVER_PASSWORD.getUrl(), new Context(Locale.getDefault(), attributesBody));
            helper.setText(mensaje, true);

            helper.addInline((String) attributesBody.get("imageLogo"), new ByteArrayResource(imageLogo.getFileBytes()), contentTypeLogo);
            helper.addInline((String) attributesBody.get("imageGreet"), new ByteArrayResource(imageGreet.getFileBytes()), contentTypeGreet);

            mailSender.send(correo);

        }  catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo electrónico", e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(true);
    }

    /**
     * Se construye un objeto MimeMessageHelper al que se le define quien es el destinatario, el asunto y los archivos anexos (si los hay).
     * Nota: El cuerpo del mensaje se define en el método donde este método es llamado y se le es pasado al objeto que aquí se retorna.
    * */
    private MimeMessageHelper buildCommonMimeMessageHelper(MimeMessage correo, String destinatario, String asunto, ArrayList<ArchivoDTO> listaArchivos) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(correo, true, "UTF-8");
        helper.setFrom(sender);
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setSentDate(new Date());
        if (listaArchivos != null){
            for (ArchivoDTO archivo : listaArchivos) {
                Resource resource = new ByteArrayResource(archivo.getFileBytes());
                helper.addAttachment(archivo.getFilename(), resource);
            }
        }
        return helper;
    }
}
