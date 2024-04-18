package co.edu.unipamplona.ciadti.cargatrabajo.services.config.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.Map;

public class ValidationErrorBuilder {
    public static ValidationError fromBindingErrors(Errors errors) {
        ValidationError error = new ValidationError("Validation failed. " + errors.getErrorCount() + " error(s)");
        for (ObjectError objectError : errors.getAllErrors()) {
            Map<String, String>   mapError = new HashMap<String, String>();
            mapError.put("entity", objectError.getObjectName());
            mapError.put("attribute", ((FieldError) objectError).getField());
            mapError.put("message", objectError.getDefaultMessage());
            error.addValidationError(mapError);
        }
        return error;
    }
}
