package com.pkmprojects.shoppiq.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Generic paginated response wrapper.
 *
 * <p>
 * Used consistently across all paginated REST endpoints to return
 * both the content page and its navigation metadata.
 * </p>
 *
 * @param content       the page items
 * @param page          zero-based page index
 * @param size          requested page size
 * @param totalElements total element count across all pages
 * @param totalPages    total number of pages
 * @param first         whether this is the first page
 * @param last          whether this is the last page
 * @param <T>           content element type
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    /**
     * Converts a Spring Data {@link Page} into a {@code PageResponse},
     * mapping each entity to a DTO via the supplied function.
     *
     * @param page   the Spring Data page
     * @param mapper entity-to-DTO mapping function
     * @param <E>    entity type
     * @param <T>    DTO type
     * @return a new PageResponse
     */
    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        List<T> content = page.getContent().stream()
                .map(mapper)
                .toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
