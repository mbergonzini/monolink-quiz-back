package monolink.monolinkquizback.controller.exceptions;

import lombok.Getter;
import monolink.monolinkquizback.controller.exceptions.dto.ApiFieldError;

import java.util.List;

@Getter
public class ApiAuthSpecificValidationException extends Exception {

    private List<ApiFieldError> errors;

    public ApiAuthSpecificValidationException(String message, List<ApiFieldError> errors) {
        super(message);
        this.errors = errors;
    }
}
