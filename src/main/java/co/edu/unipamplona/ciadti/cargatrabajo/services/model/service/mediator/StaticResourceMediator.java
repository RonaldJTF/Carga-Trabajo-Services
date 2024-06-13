package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Service
public class StaticResourceMediator {
    private final ResourceLoader resourceLoader;

    public byte[] getResourceBytes(String url){
        Resource resource = resourceLoader.getResource("classpath:" + url);
        InputStream inputStream;
        try {
            inputStream = resource.getInputStream();
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
