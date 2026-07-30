package com.pkmprojects.shoppiq.util.http;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Shared servlet request utility methods used by security handlers
 * and filters to distinguish browser from API requests.
 *
 * <p>Extracted from duplicated {@code isBrowserRequest()} methods
 * in {@code JwtAuthenticationFilter},
 * {@code ShoppiqAuthenticationEntryPoint} and
 * {@code ShoppiqAccessDeniedHandler}. This utility provides a single
 * source of truth for request type detection, ensuring consistent
 * behavior across all security components.</p>
 *
 * <p>The detection is based on the {@code Accept} header, which browsers
 * typically set to include {@code text/html}. API clients (Postman, cURL,
 * fetch) typically send {@code application/json} or omit the header.
 * This heuristic is used to determine whether to return JSON Problem
 * Detail responses or forward to the HTML error page.</p>
 *
 * @author prabhatkrmishra
 * @since 1.5.0
 */
public final class RequestUtils {

    private RequestUtils() {
    }

    /**
     * Returns {@code true} when the request originates from a browser.
     *
     * <p>Detection is based on the {@code Accept} header containing
     * {@code text/html}, which is the standard content type browsers
     * request. Pure API clients (Postman, cURL, fetch) typically send
     * {@code application/json} or omit the header.</p>
     *
     * @param request the current HTTP request
     * @return {@code true} if the client appears to be a browser
     */
    public static boolean isBrowserRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }
}
