package com.pkmprojects.shoppiq.exception.handler;

import com.pkmprojects.shoppiq.exception.constants.ProblemDetailProperties;
import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory;
import com.pkmprojects.shoppiq.exception.formatter.ValidationErrorFormatter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
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

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.boot.http.client.FilteredHostException;

import java.io.IOException;
import java.net.URI;

/**
 * <strong>Spring Boot Concept:</strong> {@code @RestControllerAdvice} class
 * that converts application and framework exceptions into RFC 9457
 * compliant {@link ProblemDetail} responses.
 *
 * <p>Uses Spring's {@code @ExceptionHandler} to target specific exception
 * types, ordered from most specific ({@link com.pkmprojects.shoppiq.exception.base.ShoppiqException})
 * to most generic ({@link Exception}). Delegates {@code ProblemDetail}
 * creation to {@link com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory}
 * and validation error formatting to
 * {@link com.pkmprojects.shoppiq.exception.formatter.ValidationErrorFormatter}.
 * The 404 handler checks the {@code Accept} header to return JSON for API
 * clients or forward to the HTML error page for browser requests.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all application-specific exceptions.
     *
     * @param exception application exception
     * @param request   current HTTP request
     * @return RFC 9457 ProblemDetail response
     */
    @ExceptionHandler(ShoppiqException.class)
    public ProblemDetail handleShoppiqException(ShoppiqException exception, HttpServletRequest request) {

        log.warn("Application exception [{}]: {}", exception.getErrorCode().getCode(), exception.getDetail());

        return ProblemDetailFactory.create(exception, createInstance(request));
    }

    /**
     * Handles Bean Validation failures.
     *
     * @param exception validation exception
     * @param request   current HTTP request
     * @return RFC 9457 ProblemDetail response
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
     * Handles requests for missing static resources (e.g. favicon.ico) and 404 errors.
     *
     * <p>
     * Forwards to the /error page so the HTML error template is rendered.
     * </p>
     *
     * @param exception NoResourceFoundException for missing resources
     * @param request   current HTTP request
     * @param response  current HTTP response
     * @return ProblemDetail for API requests, or void when forwarding for browser requests
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
     * (e.g. {@code @RequestParam @NotBlank} on controller methods).
     *
     * @param exception constraint violation exception
     * @param request   current HTTP request
     * @return RFC 9457 ProblemDetail response with 400 status
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
     * Handles missing required request parameters (e.g. a {@code @RequestParam}
     * with no default value that was not supplied in the request).
     *
     * @param exception missing parameter exception
     * @param request   current HTTP request
     * @return RFC 9457 ProblemDetail response with 400 status
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
     * Handles malformed or unreadable JSON request bodies (e.g. a JSON array
     * sent where an object is expected, or completely invalid JSON).
     *
     * @param exception message conversion failure
     * @param request   current HTTP request
     * @return RFC 9457 ProblemDetail response with 400 status
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
     * Handles type mismatch errors for request parameters
     * (e.g. invalid enum value like "VERIFIED" when expected "APPROVED").
     *
     * @param exception type mismatch exception
     * @param request   current HTTP request
     * @return RFC 9457 ProblemDetail response with 400 status
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
     * Handles client disconnections (browser navigated away, tab closed, etc.).
     *
     * <p>
     * These are expected and harmless. Logged at DEBUG level only.
     * Returns null to avoid writing a response body to a disconnected client.
     * </p>
     */
    @ExceptionHandler(ClientAbortException.class)
    public Object handleClientAbortException(ClientAbortException exception, HttpServletRequest request) {

        log.debug("Client disconnected during [{}]: {}", request.getRequestURI(), exception.getMessage());

        return null;
    }

    /**
     * Handles SSRF-blocked outbound requests.
     *
     * <p>Thrown by {@link org.springframework.boot.http.client.InetAddressFilter}
     * when an outbound HTTP request targets a blocked (internal) address.</p>
     *
     * @param exception filtered host exception
     * @param request   current HTTP request
     * @return RFC 9457 ProblemDetail response with 403 status
     */
    @ExceptionHandler(FilteredHostException.class)
    public ProblemDetail handleFilteredHostException(FilteredHostException exception, HttpServletRequest request) {

        log.warn("SSRF blocked outbound request to host '{}': {}", exception.getHost(), exception.getMessage());

        return ProblemDetailFactory.create(HttpStatus.FORBIDDEN,
                "Outbound request to '%s' is blocked by security policy.".formatted(exception.getHost()),
                ErrorCode.ACCESS_DENIED, createInstance(request));
    }

    /**
     * Handles database constraint violations (unique key, NOT NULL, foreign key).
     *
     * <p>Logs the actual SQL-level detail at WARN level for diagnostics but
     * returns a generic client-facing message to avoid leaking schema or
     * query details.</p>
     *
     * @param exception data integrity violation
     * @param request   current HTTP request
     * @return RFC 9457 ProblemDetail response with 409 status
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
     * Handles all unexpected exceptions.
     *
     * @param exception unexpected exception
     * @param request   current HTTP request
     * @return RFC 9457 ProblemDetail response
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception, HttpServletRequest request) {

        log.error("Unhandled exception while processing [{}]", request.getRequestURI(), exception);

        return ProblemDetailFactory.create(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
                ErrorCode.INTERNAL_SERVER_ERROR, createInstance(request));
    }

    /**
     * Creates the RFC 9457 instance URI.
     *
     * @param request current HTTP request
     * @return request URI
     */
    private URI createInstance(HttpServletRequest request) {
        return URI.create(request.getRequestURI());
    }

    /**
     * Determines whether the request expects a JSON response (API client)
     * versus an HTML response (browser).
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && (accept.contains("application/json") || accept.contains("application/problem+json"));
    }

    /**
     * Forwards the request to the /error page with error attributes set.
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
