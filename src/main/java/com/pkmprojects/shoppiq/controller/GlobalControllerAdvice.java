package com.pkmprojects.shoppiq.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global {@code @ControllerAdvice} that injects common model attributes into every Thymeleaf-rendered view.
 *
 * <p>Automatically adds the current request URI as {@code requestURI} so every view can
 * reference it for active navigation highlighting or UI conditioning without manual population.</p>
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
     * <p>Usage in templates:</p>
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
