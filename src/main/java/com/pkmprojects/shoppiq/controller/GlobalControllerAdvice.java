package com.pkmprojects.shoppiq.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * <strong>Spring Boot Concept:</strong> Global {@code @ControllerAdvice} that injects common
 * model attributes into every Thymeleaf-rendered view.
 *
 * <p>Automatically adds the current request URI as a {@code requestURI} model
 * attribute so every view can reference it without each controller method
 * needing to populate it manually. This is useful for highlighting the active
 * navigation item, generating form-action URLs, or conditioning UI elements
 * based on the current path.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Single responsibility</strong> — the advice only sets model
 *       attributes; it does not handle exceptions or data binding.</li>
 *   <li><strong>Transparent to controllers</strong> — controllers are unaware
 *       of this advice; the attribute is magically available in all views.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Injects the current servlet request URI into every view's model under
     * the attribute name {@code requestURI}.
     *
     * <p>This method is invoked by Spring MVC before every request handler,
     * ensuring that any Thymeleaf template can access the current URI via
     * {@code ${requestURI}} without each controller explicitly adding it.</p>
     *
     * <h4>Usage in templates:</h4>
     * <pre>{@code
     * <a th:class="${#strings.contains(requestURI, '/admin/')} ? 'active' : ''" ...>
     * }</pre>
     *
     * @param request the current HTTP request
     * @return the request URI string (e.g. {@code /categories})
     */
    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
