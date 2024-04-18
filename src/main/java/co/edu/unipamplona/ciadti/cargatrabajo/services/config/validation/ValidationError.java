package co.edu.unipamplona.ciadti.cargatrabajo.services.config.validation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ValidationError {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Map<String, String>> errors = new ArrayList<>();
    private final String errorMessage;

    public ValidationError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void addValidationError(Map<String, String> error){
        errors.add(error);
    }

    public List<Map<String, String>> getErrors() {
        return errors;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
}
