package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.admin.AdminOrderService;
import com.pkmprojects.shoppiq.service.impl.AdminOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminOrderServiceImpl Tests")
class AdminOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminOrderServiceImpl orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        Address address = Address.builder()
                .user(user)
                .label("Home")
                .fullName("Test User")
                .phone("1234567890")
                .line1("123 Test Street")
                .line2("")
                .city("Test City")
                .state("Test State")
                .country("Test Country")
                .postalCode("123456")
                .build();

        testOrder = Order.builder()
                .user(user)
                .address(address)
                .status(OrderStatus.PLACED)
                .paymentMethod(com.pkmprojects.shoppiq.enums.PaymentMethod.ONLINE)
                .paymentStatus(com.pkmprojects.shoppiq.enums.PaymentStatus.PENDING)
                .subtotal(new BigDecimal("100.00"))
                .shippingFee(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .grandTotal(new BigDecimal("100.00"))
                .placedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("getAllOrders()")
    class GetAllOrders {

        @Test
        @DisplayName("returns paginated orders")
        void returnsPaginatedOrders() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(null, 0, 20);

            assertThat(result.content()).hasSize(1);
            assertThat(result.page()).isEqualTo(0);
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("filters by status when provided")
        void filtersByStatus() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findByStatus(eq(OrderStatus.PLACED), any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(OrderStatus.PLACED, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findByStatus(eq(OrderStatus.PLACED), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getOrderById()")
    class GetOrderById {

        @Test
        @DisplayName("returns order when found")
        void returnsOrderWhenFound() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            AdminOrderResponse result = orderService.getOrderById(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("throws exception when not found")
        void throwsExceptionWhenNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(999L))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateOrderStatus()")
    class UpdateOrderStatus {

        @Test
        @DisplayName("transitions PLACED to CONFIRMED")
        void transitionsPlacedToConfirmed() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            AdminOrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions CONFIRMED to SHIPPED")
        void transitionsConfirmedToShipped() {
            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            AdminOrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions SHIPPED to OUT_FOR_DELIVERY")
        void transitionsShippedToOutForDelivery() {
            testOrder.setStatus(OrderStatus.SHIPPED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            AdminOrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.OUT_FOR_DELIVERY);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions OUT_FOR_DELIVERY to DELIVERED")
        void transitionsOutForDeliveryToDelivered() {
            testOrder.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            AdminOrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.DELIVERED);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("allows PLACED to CANCELLED")
        void allowsPlacedToCancelled() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            AdminOrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("rejects invalid transition CONFIRMED to DELIVERED")
        void rejectsInvalidTransition() {
            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects transition from DELIVERED (terminal state)")
        void rejectsTransitionFromDelivered() {
            testOrder.setStatus(OrderStatus.DELIVERED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.SHIPPED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }
    }
}