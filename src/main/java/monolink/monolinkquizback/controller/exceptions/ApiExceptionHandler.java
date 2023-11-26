package monolink.monolinkquizback.controller.exceptions;

import lombok.extern.slf4j.Slf4j;
import monolink.monolinkquizback.controller.exceptions.dto.ApiError;
import monolink.monolinkquizback.controller.exceptions.dto.ApiFieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle API exceptions for project
 * Do not work on exceptions occuring before/outside controllers scope
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    private final ApiExceptionComponent errorComponent;

    public ApiExceptionHandler(ApiExceptionComponent errorComponent) {
        this.errorComponent = errorComponent;
    }

    private static final String INTERNAL_EXCEPTION_KEY = "Internal Error";
    private static final String VALIDATION_EXCEPTION_KEY = "Validation error";
    private static final String NOTFOUND_EXCEPTION_KEY = "Resource not found";
    private static final String EXCEPTION_OCCURRED_KEY = "An exception has occurred";

    /**
     * Global method to process the catched exception
     *
     * @param ex         Exception catched
     * @param statusCode status code linked with this exception
     * @param request    request initiating the exception
     * @return the apierror object with associated status code
     */
    private ResponseEntity<ApiError> processException(Exception ex, int statusCode, WebRequest request) {
        return processException(ex, HttpStatus.valueOf(statusCode), request);
    }

    /**
     * Global method to process the catched exception
     *
     * @param ex      Exception catched
     * @param status  status linked with this exception
     * @param request request initiating the exception
     * @return the apierror object with associated status code
     */
    private ResponseEntity<ApiError> processException(Exception ex, HttpStatus status, WebRequest request) {
        return processException(ex, status, request, null);
    }

    /**
     * Global method to process the catched exception
     *
     * @param ex                   Exception catched
     * @param status               status linked with this exception
     * @param request              request initiating the exception
     * @param overrideErrorMessage message overriding default error message from exception
     * @return the apierror object with associated status code
     */
    private ResponseEntity<ApiError> processException(Exception ex, HttpStatus status, WebRequest request, String overrideErrorMessage) {
        log.error(EXCEPTION_OCCURRED_KEY, ex);
        String errorMessage = ex.getMessage();
        if (overrideErrorMessage != null) {
            errorMessage = overrideErrorMessage;
        }
        ApiError error = errorComponent.buildApiErrorObject(request, status, errorMessage);
        return new ResponseEntity<>(error, status);
    }

    /**
     * Handle MissingServletRequestParameterException. Triggered when a 'required'
     * request parameter is missing.
     *
     * @param ex      MissingServletRequestParameterException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiError> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            WebRequest request) {
        return processException(ex, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is
     * invalid as well.
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ApiError> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            WebRequest request) {
        return processException(ex, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request, INTERNAL_EXCEPTION_KEY);
    }

    /**
     * Handle MethodArgumentNotValidException. Triggered when an object fails @Valid
     * validation.
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid
     *                validation fails
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiError handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        log.error(EXCEPTION_OCCURRED_KEY, ex);

        List<ApiFieldError> errors = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        for (ObjectError bindingError : ex.getBindingResult().getGlobalErrors()) {
            messages.add(bindingError.getDefaultMessage());
        }

        for (FieldError bindingError : ex.getBindingResult().getFieldErrors()) {
            ApiFieldError fieldError = new ApiFieldError(bindingError.getField(),
                    bindingError.getDefaultMessage());
            errors.add(fieldError);
        }

        return errorComponent.buildApiErrorWithFields(request, HttpStatus.BAD_REQUEST,
                VALIDATION_EXCEPTION_KEY, errors);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ApiAuthSpecificValidationException.class)
    protected ApiError handleMethodArgumentNotValid(
            ApiAuthSpecificValidationException ex,
            WebRequest request) {
        log.error(EXCEPTION_OCCURRED_KEY, ex);

        List<ApiFieldError> errors = ex.getErrors();

        return errorComponent.buildApiErrorWithFields(request, HttpStatus.BAD_REQUEST,
                VALIDATION_EXCEPTION_KEY, errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<ApiError> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request) {
        return processException(ex, HttpStatus.UNAUTHORIZED, request, "Le pseudo ou le mot de passe est incorrect");
    }


    /**
     * Handle HttpMessageNotReadableException. Happens when request JSON is
     * malformed.
     *
     * @param ex      HttpMessageNotReadableException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                    WebRequest request) {
        return processException(ex, HttpStatus.BAD_REQUEST, request, INTERNAL_EXCEPTION_KEY);
    }

}