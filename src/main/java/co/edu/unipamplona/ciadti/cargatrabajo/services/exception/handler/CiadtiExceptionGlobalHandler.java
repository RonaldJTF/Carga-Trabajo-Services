package co.edu.unipamplona.ciadti.cargatrabajo.services.exception.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;

@Log4j2
@ControllerAdvice
public class CiadtiExceptionGlobalHandler {
    @ExceptionHandler(CiadtiException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<?> handleException(CiadtiException ex) {
        log.error("[" + 500 + "] " + ex.getMessage());
        ex.printStackTrace();
        return Methods.handleRapException(ex);
    }
}