package com.pkmprojects.shoppiq.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Generic paginated response wrapper used across all paginated endpoints.
 *
 * <p>This record wraps a list of content items together with pagination
 * navigation metadata. It is returned by every paginated REST endpoint
 * in the application, providing a consistent response structure that
 * the frontend can rely on for rendering pagination controls and
 * infinite-scroll mechanisms.</p>
 *
 * <p>The static {@link #of(Page, Function)} factory method converts a
 * Spring Data {@code Page} entity directly into this DTO, applying an
 * entity-to-DTO mapping function to each element. An overloaded
 * {@link #of(List, int, int, long, Function)} variant accepts raw
 * pagination data for cases where the content list is computed
 * independently of Spring Data.</p>
 *
 * @param content       the page items, already mapped to DTOs via the
 *                      supplied mapping function
 * @param page          zero-based page index (0 = first page)
 * @param size          requested page size (maximum items per page)
 * @param totalElements total number of elements across all pages;
 *                      used to calculate total page count
 * @param totalPages    total number of pages available
 * @param first         whether this is the first page (true when page = 0)
 * @param last          whether this is the last page (true when no more pages follow)
 * @param <T>           the content element type, typically a response DTO
 * @author prabhatkrmishra
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

    /**
     * Creates a {@code PageResponse} from raw pagination data,
     * mapping each entity to a DTO via the supplied function.
     *
     * @param content       the page items
     * @param page          zero-based page index
     * @param size          requested page size
     * @param totalElements total element count across all pages
     * @param mapper        entity-to-DTO mapping function
     * @param <E>           entity type
     * @param <T>           DTO type
     * @return a new PageResponse
     */
    public static <E, T> PageResponse<T> of(List<E> content, int page, int size,
                                            long totalElements, Function<E, T> mapper) {
        List<T> mapped = content.stream().map(mapper).toList();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageResponse<>(
                mapped, page, size, totalElements, totalPages,
                page == 0, page >= totalPages - 1
        );
    }
}
