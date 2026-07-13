package com.pkmprojects.shoppiq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.config.JacksonConfig;
import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.config.SecurityConfig;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.request.CategoryRequest;
import com.pkmprojects.shoppiq.dto.response.CategoryResponse;
import com.pkmprojects.shoppiq.exception.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.DuplicateCategoryException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.CategoryService;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Controller-slice tests for {@link CategoryController}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@WebMvcTest(CategoryController.class)
@Import({
        SecurityConfig.class,
        JacksonConfig.class,
        GlobalExceptionHandler.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationUtils.class,
        JwtCookieFactory.class,
        ShoppiqAuthenticationEntryPoint.class,
        ShoppiqAccessDeniedHandler.class,
        ProblemDetailResponseWriter.class
})
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @MockitoBean
    private PaginationProperties paginationProperties;

    private static CategoryResponse stubResponse(Long id, String name, String slug) {
        return new CategoryResponse(id, name, slug, "Description of " + name);
    }

    // ---------------------------------------------------------------
    // POST /categories
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("POST /categories")
    class CreateCategory {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 201 with the created category")
        void create_validRequest_returns201() throws Exception {
            CategoryRequest request = new CategoryRequest("Electronics", "Gadgets and devices");
            CategoryResponse response = stubResponse(1L, "Electronics", "electronics");

            when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

            mockMvc.perform(post("/categories").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Electronics"))
                    .andExpect(jsonPath("$.slug").value("electronics"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 409 when category name already exists")
        void create_duplicateName_returns409() throws Exception {
            CategoryRequest request = new CategoryRequest("Electronics", "Duplicate category");

            when(categoryService.create(any(CategoryRequest.class)))
                    .thenThrow(DuplicateCategoryException.category("Electronics"));

            mockMvc.perform(post("/categories").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("CATEGORY-409-001"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 400 when category name is blank")
        void create_blankName_returns400() throws Exception {
            CategoryRequest request = new CategoryRequest("", "Some description");

            mockMvc.perform(post("/categories").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(categoryService, never()).create(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 400 when description is blank")
        void create_blankDescription_returns400() throws Exception {
            CategoryRequest request = new CategoryRequest("Electronics", "");

            mockMvc.perform(post("/categories").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 400 when name exceeds 100 characters")
        void create_nameTooLong_returns400() throws Exception {
            String longName = "A".repeat(101);
            CategoryRequest request = new CategoryRequest(longName, "Valid desc");

            mockMvc.perform(post("/categories").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));
        }
    }

    // ---------------------------------------------------------------
    // PUT /categories/{id}/update
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("PUT /categories/{id}/update")
    class UpdateCategory {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with updated category on success")
        void update_validRequest_returns200() throws Exception {
            CategoryRequest request = new CategoryRequest("Fashion", "Latest trends");
            CategoryResponse response = stubResponse(1L, "Fashion", "fashion");

            when(categoryService.update(eq(1L), any(CategoryRequest.class))).thenReturn(response);

            mockMvc.perform(put("/categories/1/update").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Fashion"))
                    .andExpect(jsonPath("$.slug").value("fashion"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 404 when category does not exist")
        void update_notFound_returns404() throws Exception {
            CategoryRequest request = new CategoryRequest("Fashion", "Latest trends");

            when(categoryService.update(eq(999L), any(CategoryRequest.class)))
                    .thenThrow(CategoryNotFoundException.id(999L));

            mockMvc.perform(put("/categories/999/update").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("CATEGORY-404-001"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 409 when new name conflicts with another category")
        void update_duplicateName_returns409() throws Exception {
            CategoryRequest request = new CategoryRequest("Fashion", "Clothing");

            when(categoryService.update(eq(1L), any(CategoryRequest.class)))
                    .thenThrow(DuplicateCategoryException.category("Fashion"));

            mockMvc.perform(put("/categories/1/update").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("CATEGORY-409-001"));
        }
    }

    // ---------------------------------------------------------------
    // DELETE /categories/{id}/delete
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /categories/{id}/delete")
    class DeleteCategory {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 204 on successful deletion")
        void delete_exists_returns204() throws Exception {
            doNothing().when(categoryService).delete(1L);

            mockMvc.perform(delete("/categories/1/delete").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(categoryService).delete(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 404 when category does not exist")
        void delete_notFound_returns404() throws Exception {
            doThrow(CategoryNotFoundException.id(999L)).when(categoryService).delete(999L);

            mockMvc.perform(delete("/categories/999/delete").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("CATEGORY-404-001"));
        }
    }

    // ---------------------------------------------------------------
    // GET /categories/{id}  — REMOVED (use slug instead)
    // ---------------------------------------------------------------

    // ---------------------------------------------------------------
    // GET /categories/slug/{slug}
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("GET /categories/slug/{slug}")
    class GetBySlug {

        @Test
        @WithMockUser
        @DisplayName("Returns 200 with the matching category")
        void getBySlug_found_returns200() throws Exception {
            when(categoryService.getBySlug("electronics"))
                    .thenReturn(stubResponse(1L, "Electronics", "electronics"));

            mockMvc.perform(get("/categories/slug/electronics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.slug").value("electronics"));
        }

        @Test
        @WithMockUser
        @DisplayName("Returns 404 when slug does not match any category")
        void getBySlug_notFound_returns404() throws Exception {
            when(categoryService.getBySlug("unknown"))
                    .thenThrow(CategoryNotFoundException.slug("unknown"));

            mockMvc.perform(get("/categories/slug/unknown"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("CATEGORY-404-001"));
        }
    }

    // ---------------------------------------------------------------
    // GET /categories/all
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("GET /categories/all")
    class GetAll {

        @Test
        @WithMockUser
        @DisplayName("Returns 200 with all categories")
        void getAll_multipleCategories_returnsList() throws Exception {
            when(categoryService.getAll()).thenReturn(List.of(
                    stubResponse(1L, "Electronics", "electronics"),
                    stubResponse(2L, "Fashion", "fashion")
            ));

            mockMvc.perform(get("/categories/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Electronics"))
                    .andExpect(jsonPath("$[1].name").value("Fashion"));
        }

        @Test
        @WithMockUser
        @DisplayName("Returns 200 with an empty list when no categories exist")
        void getAll_noCategories_returnsEmptyList() throws Exception {
            when(categoryService.getAll()).thenReturn(List.of());

            mockMvc.perform(get("/categories/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ---------------------------------------------------------------
    // GET /categories/all/paged
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("GET /categories/all/paged")
    class GetAllPaginated {

        @Test
        @WithMockUser
        @DisplayName("Returns 200 with paged category data")
        void getAllPaginated_withData_returns200() throws Exception {
            PageResponse<CategoryResponse> pageResponse = new PageResponse<>(
                    List.of(stubResponse(1L, "Electronics", "electronics")),
                    0, 20, 1, 1, true, false
            );

            when(paginationProperties.maxPageSize()).thenReturn(100);
            when(categoryService.getAll(0, 20, null)).thenReturn(pageResponse);

            mockMvc.perform(get("/categories/all/paged")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Electronics"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("Returns 200 with empty content when no categories exist")
        void getAllPaginated_empty_returns200() throws Exception {
            PageResponse<CategoryResponse> emptyPage = new PageResponse<>(
                    List.of(), 0, 20, 0, 0, true, true
            );

            when(paginationProperties.maxPageSize()).thenReturn(100);
            when(categoryService.getAll(0, 20, null)).thenReturn(emptyPage);

            mockMvc.perform(get("/categories/all/paged")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("Uses default page=0 and size=20 when no params provided")
        void getAllPaginated_defaultParams_returns200() throws Exception {
            PageResponse<CategoryResponse> pageResponse = new PageResponse<>(
                    List.of(), 0, 20, 0, 0, true, true
            );

            when(paginationProperties.maxPageSize()).thenReturn(100);
            when(categoryService.getAll(0, 20, null)).thenReturn(pageResponse);

            mockMvc.perform(get("/categories/all/paged"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20));

            verify(categoryService).getAll(0, 20, null);
        }
    }
}