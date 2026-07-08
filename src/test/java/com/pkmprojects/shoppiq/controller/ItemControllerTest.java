package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.config.JacksonConfig;
import com.pkmprojects.shoppiq.config.SecurityConfig;
import com.pkmprojects.shoppiq.dto.response.CategoryResponse;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.ItemService;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
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
@DisplayName("ItemController Tests")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private static final Long ITEM_ID = 1L;
    private static final String ITEM_NAME = "Test Product";
    private static final String ITEM_DESC = "A test product description";
    private static final String BRAND = "TestBrand";
    private static final String SKU = "TST-001";
    private static final BigDecimal PRICE = new BigDecimal("99.99");
    private static final int STOCK = 10;
    private static final BigDecimal DISCOUNT = BigDecimal.ZERO;
    private static final Long CATEGORY_ID = 1L;

    private static ItemResponse stubResponse(Long id) {
        return new ItemResponse(
                ProductPublishingStatus.DRAFT,
                id, id, ITEM_NAME, ITEM_DESC, BRAND, SKU, PRICE, STOCK, DISCOUNT,
                null,
                new CategoryResponse(CATEGORY_ID, "Electronics", "electronics", "Gadgets"),
                Instant.now(), Instant.now()
        );
    }

    @BeforeEach
    void setUp() {
        User authenticatedUser = User.builder()
                .name("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("hashed")
                .enabled(true)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /items/all")
    class GetAllItems {

        @Test
        @DisplayName("Returns 200 with list of items")
        void getAll_returnsList() throws Exception {
            when(itemService.getAll())
                    .thenReturn(List.of(stubResponse(1L), stubResponse(2L)));

            mockMvc.perform(get("/items/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));
        }
    }

    @Nested
    @DisplayName("GET /items/{id}")
    class GetItemById {

        @Test
        @DisplayName("Returns 200 with item when found")
        void getById_found_returns200() throws Exception {
            when(itemService.getById(eq(1L)))
                    .thenReturn(stubResponse(1L));

            mockMvc.perform(get("/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value(ITEM_NAME));
        }

        @Test
        @DisplayName("Returns 404 when item is not found")
        void getById_notFound_returns404() throws Exception {
            when(itemService.getById(eq(99L)))
                    .thenThrow(ItemNotFoundException.id(99L));

            mockMvc.perform(get("/items/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-404-001"));
        }
    }
}
