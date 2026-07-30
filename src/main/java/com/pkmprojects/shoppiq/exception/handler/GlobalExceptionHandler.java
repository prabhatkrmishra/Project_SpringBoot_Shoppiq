package com.pkmprojects.shoppiq.exception.handler;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.constants.ProblemDetailProperties;
import com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory;
import com.pkmprojects.shoppiq.exception.formatter.ValidationErrorFormatter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.boot.http.client.FilteredHostException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.net.URI;

/**
 * Central exception handler that converts application and framework exceptions
 * into RFC 9457 compliant {@link ProblemDetail} responses.
 *
 * <p>This {@code @RestControllerAdvice} class intercepts all exceptions thrown
 * by controller methods and translates them into structured HTTP responses.
 * It uses Spring's {@code @ExceptionHandler} mechanism to target specific
 * exception types, ordered from most specific ({@link ShoppiqException}) to
 * most generic ({@link Exception}). Each handler delegates to
 * {@link ProblemDetailFactory} for Problem Detail creation and
 * {@link ValidationErrorFormatter} for validation error formatting.</p>
 *
 * <p>The handler implements a content-negotiation strategy for 404 errors:
 * API clients (those sending {@code Accept: application/json}) receive a
 * JSON Problem Detail response, while browser requests are forwarded to the
 * {@code /error} endpoint for HTML error page rendering. This dual behavior
 * ensures that both SPA frontends and traditional browser users see
 * appropriate error pages.</p>
 *
 * @author prabhatkrmishra
 * @see ProblemDetailFactory
 * @see ValidationErrorFormatter
 * @see ShoppiqException
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all application-specific exceptions thrown by service and
     * controller layers.
     *
     * <p>This is the primary handler for any exception that extends
     * {@link ShoppiqException}. It extracts the error code, HTTP status,
     * and detail message from the exception and delegates to
     * {@link ProblemDetailFactory} to create the response. The exception
     * is logged at WARN level with the error code for diagnostics.</p>
     *
     * @param exception the application-specific exception
     * @param request   the current HTTP request
     * @return an RFC 9457 Problem Detail response
     */
    @ExceptionHandler(ShoppiqException.class)
    public ProblemDetail handleShoppiqException(ShoppiqException exception, HttpServletRequest request) {

        log.warn("Application exception [{}]: {}", exception.getErrorCode().getCode(), exception.getDetail());

        return ProblemDetailFactory.create(exception, createInstance(request));
    }

    /**
     * Handles Bean Validation failures from {@code @Valid} and {@code @Validated}
     * annotations on controller method parameters.
     *
     * <p>Collects all field-level and global-level validation errors from the
     * {@link org.springframework.validation.BindingResult} and formats them
     * into a human-readable string using {@link ValidationErrorFormatter}.
     * The formatted message is included in the Problem Detail response at
     * HTTP 400 status. Validation errors are logged at DEBUG level to avoid
     * noise in production logs.</p>
     *
     * @param exception the validation exception containing binding errors
     * @param request   the current HTTP request
     * @return an RFC 9457 Problem Detail response with 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {

        String detail = ValidationErrorFormatter.format(
                exception.getBindingResult()
        );

        log.debug("Validation failed: {}", detail);

        return ProblemDetailFactory.create(HttpStatus.BAD_REQUEST, detail,
                ErrorCode.VALIDATION_FAILED, createInstance(request));
    }

    /**
     * Handles requests for missing static resources (e.g., favicon.ico)
     * and undefined API endpoints.
     *
     * <p>This handler implements content negotiation: if the request
     * includes an {@code Accept} header containing {@code application/json}
     * or {@code application/problem+json}, it returns a JSON Problem Detail
     * response. For browser requests (no JSON Accept header), it forwards
     * to the {@code /error} endpoint so the HTML error template is rendered.
     * This ensures that API clients and browser users both receive
     * appropriate error responses.</p>
     *
     * @param exception the exception indicating a missing resource
     * @param request   the current HTTP request
     * @param response  the current HTTP response
     * @return a Problem Detail for API requests, or void when forwarding for browser requests
     * @throws IOException if forwarding to the error page fails
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFoundException(NoResourceFoundException exception,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws IOException {

        log.debug("Resource not found: [{}]", request.getRequestURI());

        ProblemDetail problemDetail = ProblemDetailFactory.create(HttpStatus.NOT_FOUND,
                "Resource not found",
                ErrorCode.RESOURCE_NOT_FOUND, createInstance(request));

        if (isApiRequest(request)) {
            return problemDetail;
        }

        forwardToErrorPage(request, response, problemDetail);
        return null;
    }

    /**
     * Handles constraint violations from {@code @Validated} method parameters
     * such as {@code @RequestParam @NotBlank} on controller methods.
     *
     * <p>Unlike {@code @Valid} on request body parameters, constraint
     * violations on method parameters throw
     * {@link ConstraintViolationException} instead of
     * {@link MethodArgumentNotValidException}. This handler formats the
     * violation message and returns a Problem Detail response at HTTP 400.</p>
     *
     * @param exception the constraint violation exception
     * @param request   the current HTTP request
     * @return an RFC 9457 Problem Detail response with 400 status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(
            ConstraintViolationException exception, HttpServletRequest request) {

        log.debug("Constraint violation [{}]: {}", request.getRequestURI(), exception.getMessage());

        return ProblemDetailFactory.create(HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                ErrorCode.VALIDATION_FAILED, createInstance(request));
    }

    /**
     * Handles missing required request parameters such as a
     * {@code @RequestParam} with no default value that was not supplied.
     *
     * <p>This exception is thrown by Spring MVC when a controller method
     * declares a required request parameter that is absent from the
     * incoming request. The handler formats a clear message indicating
     * which parameter is missing and returns HTTP 400.</p>
     *
     * @param exception the missing parameter exception
     * @param request   the current HTTP request
     * @return an RFC 9457 Problem Detail response with 400 status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception, HttpServletRequest request) {

        log.debug("Missing request parameter [{}]: {}", request.getRequestURI(), exception.getMessage());

        return ProblemDetailFactory.create(HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                ErrorCode.VALIDATION_FAILED, createInstance(request));
    }

    /**
     * Handles malformed or unreadable JSON request bodies.
     *
     * <p>This exception is thrown when the request body cannot be
     * deserialized by Jackson, typically because the JSON is
     * syntactically invalid or a JSON array was sent where an object
     * was expected. The handler returns a generic message to avoid
     * leaking parser details, at HTTP 400 status.</p>
     *
     * @param exception the message conversion failure
     * @param request   the current HTTP request
     * @return an RFC 9457 Problem Detail response with 400 status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception, HttpServletRequest request) {

        log.debug("Unreadable HTTP message [{}]: {}", request.getRequestURI(), exception.getMessage());

        return ProblemDetailFactory.create(HttpStatus.BAD_REQUEST,
                "Malformed or unreadable request body.",
                ErrorCode.VALIDATION_FAILED, createInstance(request));
    }

    /**
     * Handles type mismatch errors for request parameters.
     *
     * <p>This exception is thrown when a request parameter cannot be
     * converted to the expected type, such as providing "VERIFIED" when
     * an enum value of "APPROVED" is expected. The handler formats a
     * message indicating the invalid value, the parameter name, and the
     * expected type, at HTTP 400 status.</p>
     *
     * @param exception the type mismatch exception
     * @param request   the current HTTP request
     * @return an RFC 9457 Problem Detail response with 400 status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {

        String paramName = exception.getName();
        String invalidValue = exception.getValue() != null ? exception.getValue().toString() : "";
        Class<?> requiredType = exception.getRequiredType();
        String typeName = requiredType != null ? requiredType.getSimpleName() : "unknown";

        String detail = "Invalid value '%s' for parameter '%s'. Expected type: %s."
                .formatted(invalidValue, paramName, typeName);

        log.debug("Type mismatch [{}]: {}", request.getRequestURI(), detail);

        return ProblemDetailFactory.create(HttpStatus.BAD_REQUEST,
                detail,
                ErrorCode.VALIDATION_FAILED, createInstance(request));
    }

    /**
     * Handles client disconnections such as browser navigation, tab closure,
     * or network interruption during request processing.
     *
     * <p>These exceptions are expected and harmless in production. They occur
     * when the client disconnects before the server can write the response.
     * The handler logs at DEBUG level only and returns null to avoid writing
     * a response body to a disconnected client, which would cause an
     * {@link java.io.IOException}.</p>
     *
     * @param exception the client abort exception
     * @param request   the current HTTP request
     * @return null to skip response writing
     */
    @ExceptionHandler(ClientAbortException.class)
    public Object handleClientAbortException(ClientAbortException exception, HttpServletRequest request) {

        log.debug("Client disconnected during [{}]: {}", request.getRequestURI(), exception.getMessage());

        return null;
    }

    /**
     * Handles SSRF-blocked outbound requests.
     *
     * <p>This exception is thrown by {@link org.springframework.boot.http.client.InetAddressFilter}
     * when an outbound HTTP request targets a blocked (internal) address.
     * The handler logs the blocked host at WARN level and returns a 403
     * Forbidden response to prevent the application from being used as a
     * proxy to internal networks.</p>
     *
     * @param exception the filtered host exception
     * @param request   the current HTTP request
     * @return an RFC 9457 Problem Detail response with 403 status
     */
    @ExceptionHandler(FilteredHostException.class)
    public ProblemDetail handleFilteredHostException(FilteredHostException exception, HttpServletRequest request) {

        log.warn("SSRF blocked outbound request to host '{}': {}", exception.getHost(), exception.getMessage());

        return ProblemDetailFactory.create(HttpStatus.FORBIDDEN,
                "Outbound request to '%s' is blocked by security policy.".formatted(exception.getHost()),
                ErrorCode.ACCESS_DENIED, createInstance(request));
    }

    /**
     * Handles database constraint violations such as unique key, NOT NULL,
     * or foreign key violations.
     *
     * <p>The actual SQL-level detail is logged at WARN level for diagnostics,
     * but the client receives a generic message to avoid leaking database
     * schema or query details. The response uses HTTP 409 Conflict status
     * to indicate that the request conflicts with existing data.</p>
     *
     * @param exception the data integrity violation exception
     * @param request   the current HTTP request
     * @return an RFC 9457 Problem Detail response with 409 status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(
            DataIntegrityViolationException exception, HttpServletRequest request) {

        log.warn("Data integrity violation [{}]: {}", request.getRequestURI(), exception.getMostSpecificCause().getMessage());

        return ProblemDetailFactory.create(HttpStatus.CONFLICT,
                "The request conflicts with existing data. Please check for duplicates or missing required fields.",
                ErrorCode.DATA_INTEGRITY_VIOLATION, createInstance(request));
    }

    /**
     * Handles all unexpected exceptions that are not caught by any other handler.
     *
     * <p>This is the catch-all handler for any exception that slips through
     * the more specific handlers. It logs the full exception stack trace at
     * ERROR level for post-mortem analysis and returns a generic HTTP 500
     * response with no internal details to avoid leaking sensitive
     * information.</p>
     *
     * @param exception the unexpected exception
     * @param request   the current HTTP request
     * @return an RFC 9457 Problem Detail response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception, HttpServletRequest request) {

        log.error("Unhandled exception while processing [{}]", request.getRequestURI(), exception);

        return ProblemDetailFactory.create(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
                ErrorCode.INTERNAL_SERVER_ERROR, createInstance(request));
    }

    /**
     * Creates the RFC 9457 instance URI from the current request.
     *
     * <p>The instance URI identifies the specific occurrence of the error
     * and is set to the request URI. This allows clients to correlate
     * error responses with the requests that produced them.</p>
     *
     * @param request the current HTTP request
     * @return the request URI as an {@link URI} instance
     */
    private URI createInstance(HttpServletRequest request) {
        return URI.create(request.getRequestURI());
    }

    /**
     * Determines whether the request expects a JSON response (API client)
     * versus an HTML response (browser).
     *
     * <p>Checks the {@code Accept} header for {@code application/json} or
     * {@code application/problem+json} content types. Returns true for
     * API clients and false for browser requests, enabling the 404 handler
     * to choose between returning a Problem Detail response or forwarding
     * to the HTML error page.</p>
     *
     * @param request the current HTTP request
     * @return true if the request is from an API client, false otherwise
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && (accept.contains("application/json") || accept.contains("application/problem+json"));
    }

    /**
     * Forwards the request to the {@code /error} page with error attributes
     * set for the HTML error template.
     *
     * <p>This method sets the HTTP status code, error message, and error
     * code as request attributes that the Thymeleaf error template can
     * read and render. If forwarding fails (e.g., the error page itself
     * throws an exception), it falls back to sending a raw 404 error.</p>
     *
     * @param request       the current HTTP request
     * @param response      the current HTTP response
     * @param problemDetail the Problem Detail to extract error attributes from
     * @throws IOException if forwarding or error sending fails
     */
    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response,
                                    ProblemDetail problemDetail) throws IOException {
        try {
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, problemDetail.getStatus());
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, problemDetail.getDetail());
            request.setAttribute(ProblemDetailProperties.ERROR_CODE, problemDetail.getProperties() != null
                    ? problemDetail.getProperties().get(ProblemDetailProperties.ERROR_CODE) : null);
            request.getRequestDispatcher("/error").forward(request, response);
        } catch (Exception e) {
            log.error("Failed to forward to /error page", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
        }
    }
}
