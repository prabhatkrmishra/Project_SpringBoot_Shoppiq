package com.pkmprojects.shoppiq.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for converting human-readable text into URL-friendly slugs.
 *
 * <p>
 * A slug is a lowercase, normalized, URL-safe representation of text.
 * Slugs are commonly used to build readable URLs for categories, products,
 * blog posts, and other resources.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Normalize Unicode text.</li>
 *     <li>Remove diacritical marks.</li>
 *     <li>Replace whitespace with hyphens.</li>
 *     <li>Remove unsupported characters.</li>
 *     <li>Collapse duplicate hyphens.</li>
 *     <li>Convert text to lowercase.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>This class performs <strong>only text transformation</strong>.</li>
 *     <li>It does <strong>not</strong> guarantee slug uniqueness.</li>
 *     <li>Slug uniqueness must be enforced by the service layer.</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <pre>
 * Electronics & Gadgets  -> electronics-gadgets
 * Men's Fashion          -> mens-fashion
 * Café & Bistro          -> cafe-bistro
 * Home Appliances        -> home-appliances
 * </pre>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>
 * This utility is completely stateless and thread-safe.
 * All regular expressions are immutable and precompiled.
 * </p>
 *
 * @author PrabhatKrMishra
 * @see com.pkmprojects.shoppiq.service.CategoryService
 * @since 1.0.0
 */
public final class SlugUtil {

    /**
     * Replacement used for whitespace.
     */
    private static final String DASH = "-";

    /**
     * Empty string constant.
     */
    private static final String EMPTY = "";

    /**
     * Matches one or more whitespace characters.
     */
    private static final Pattern WHITESPACE =
            Pattern.compile("\\s+");

    /**
     * Matches Unicode combining diacritical marks.
     */
    private static final Pattern DIACRITICS =
            Pattern.compile("\\p{M}+");

    /**
     * Matches unsupported slug characters.
     */
    private static final Pattern NON_LATIN =
            Pattern.compile("[^\\w-]");

    /**
     * Matches repeated hyphens.
     */
    private static final Pattern MULTIPLE_DASH =
            Pattern.compile("-{2,}");

    /**
     * Matches leading and trailing hyphens.
     */
    private static final Pattern EDGE_DASH =
            Pattern.compile("^-|-$");

    /**
     * Matches a valid slug.
     */
    private static final Pattern VALID_SLUG =
            Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    /**
     * Prevents instantiation.
     */
    private SlugUtil() {
        throw new AssertionError("Utility class cannot be instantiated.");
    }

    /**
     * Converts the supplied text into a URL-friendly slug.
     *
     * <p>
     * This method performs deterministic text normalization only.
     * It does not check whether the resulting slug already exists.
     * </p>
     *
     * @param input source text
     * @return normalized slug
     * @throws IllegalArgumentException if the supplied text is null,
     *                                  blank, or produces an empty slug
     */
    public static String toSlug(String input) {

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                    "Input cannot be null or blank."
            );
        }

        String slug = Normalizer.normalize(input, Normalizer.Form.NFD);

        slug = DIACRITICS.matcher(slug).replaceAll(EMPTY);
        slug = WHITESPACE.matcher(slug).replaceAll(DASH);
        slug = NON_LATIN.matcher(slug).replaceAll(EMPTY);
        slug = MULTIPLE_DASH.matcher(slug).replaceAll(DASH);
        slug = slug.toLowerCase(Locale.ROOT);
        slug = EDGE_DASH.matcher(slug).replaceAll(EMPTY);

        if (slug.isBlank()) {
            throw new IllegalArgumentException(
                    "Input produced an empty slug."
            );
        }

        return slug;
    }

    /**
     * Determines whether the supplied value is already a valid slug.
     *
     * @param value value to validate
     * @return {@code true} if the value is a valid slug
     */
    public static boolean isSlug(String value) {
        return value != null
                && VALID_SLUG.matcher(value).matches();
    }
}