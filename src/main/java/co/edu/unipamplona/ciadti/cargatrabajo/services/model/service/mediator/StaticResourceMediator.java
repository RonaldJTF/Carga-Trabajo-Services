package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Trace;

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
            Trace.logError(this.getClass().getName(), Methods.getCurrentMethodName(this.getClass()), e);
        }
        return null;
    }

    public Resource getResource(String url){
        return resourceLoader.getResource("classpath:" + url);
    }
}
