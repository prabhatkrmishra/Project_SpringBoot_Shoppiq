package com.pkmprojects.shoppiq.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SlugUtil}.
 *
 * <p>Verifies all text transformation cases and guard conditions.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DisplayName("SlugUtil Tests")
class SlugUtilTest {

    // ---------------------------------------------------------------
    // toSlug()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("toSlug()")
    class ToSlug {

        @ParameterizedTest(name = "''{0}'' → ''{1}''")
        @CsvSource({
                "Electronics,                 electronics",
                "Home Appliances,             home-appliances",
                "Men's Fashion,               mens-fashion",
                "Electronics & Gadgets,       electronics-gadgets",
                "Café & Bistro,               cafe-bistro",
                "  Spaces Around  ,           spaces-around",
                "Multiple   Spaces,           multiple-spaces",
                "Already-Slug,                already-slug",
                "UPPERCASE,                   uppercase",
                "Mixed CASE input,            mixed-case-input",
                "hyphen--double,              hyphen-double",
        })
        @DisplayName("Produces the expected slug for common inputs")
        void toSlug_commonInputs_producesExpectedSlug(String input, String expected) {
            assertThat(SlugUtil.toSlug(input.trim())).isEqualTo(expected.trim());
        }

        @Test
        @DisplayName("Strips leading and trailing hyphens")
        void toSlug_leadingTrailingSpecialChars_stripsEdgeDashes() {
            // Input that after transformation would start/end with dashes
            String result = SlugUtil.toSlug("!Hello!");
            assertThat(result).doesNotStartWith("-");
            assertThat(result).doesNotEndWith("-");
        }

        @Test
        @DisplayName("Returns lowercase output regardless of input case")
        void toSlug_mixedCase_returnsLowercase() {
            assertThat(SlugUtil.toSlug("UPPER LOWER")).isEqualTo("upper-lower");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Throws IllegalArgumentException for null, empty, or blank input")
        void toSlug_nullOrBlank_throwsIllegalArgumentException(String input) {
            assertThatThrownBy(() -> SlugUtil.toSlug(input))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when input produces an empty slug")
        void toSlug_inputReducesToEmpty_throwsIllegalArgumentException() {
            // Only special characters that get stripped entirely
            assertThatThrownBy(() -> SlugUtil.toSlug("!!!"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Normalizes diacritical characters")
        void toSlug_diacritics_normalized() {
            assertThat(SlugUtil.toSlug("Ångström")).isEqualTo("angstrom");
        }
    }

    // ---------------------------------------------------------------
    // isSlug()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("isSlug()")
    class IsSlug {

        @ParameterizedTest(name = "''{0}'' is a valid slug")
        @ValueSource(strings = {
                "electronics",
                "home-appliances",
                "abc123",
                "a",
                "slug-with-numbers-123",
        })
        @DisplayName("Returns true for valid slugs")
        void isSlug_validSlugs_returnsTrue(String slug) {
            assertThat(SlugUtil.isSlug(slug)).isTrue();
        }

        @ParameterizedTest(name = "''{0}'' is NOT a valid slug")
        @ValueSource(strings = {
                "With Space",
                "UPPERCASE",
                "-leading-dash",
                "trailing-dash-",
                "double--dash",
                "special!chars",
                "",
        })
        @DisplayName("Returns false for invalid slugs")
        void isSlug_invalidSlugs_returnsFalse(String slug) {
            assertThat(SlugUtil.isSlug(slug)).isFalse();
        }

        @Test
        @DisplayName("Returns false for null input")
        void isSlug_null_returnsFalse() {
            assertThat(SlugUtil.isSlug(null)).isFalse();
        }
    }

    // ---------------------------------------------------------------
    // Utility class guard
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Cannot be instantiated via reflection")
    void constructor_throwsAssertionError() {
        assertThatThrownBy(() -> {
            var constructor = SlugUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(AssertionError.class);
    }
}