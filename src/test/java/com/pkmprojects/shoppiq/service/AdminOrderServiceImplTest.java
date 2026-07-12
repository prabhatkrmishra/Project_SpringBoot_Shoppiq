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

        @Test
        @DisplayName("transitions PLACED to CANCEL_REQUEST")
        void transitionsPlacedToCancelRequest() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.CANCEL_REQUEST);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions CANCEL_REQUEST to CANCELLED")
        void transitionsCancelRequestToCancelled() {
            testOrder.setStatus(OrderStatus.CANCEL_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions DELIVERED to RETURN_REQUEST")
        void transitionsDeliveredToReturnRequest() {
            testOrder.setStatus(OrderStatus.DELIVERED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.RETURN_REQUEST);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions DELIVERED to REFUND_REQUEST")
        void transitionsDeliveredToRefundRequest() {
            testOrder.setStatus(OrderStatus.DELIVERED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.REFUND_REQUEST);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions DELIVERED to REPLACE_REQUEST")
        void transitionsDeliveredToReplaceRequest() {
            testOrder.setStatus(OrderStatus.DELIVERED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.REPLACE_REQUEST);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions RETURN_REQUEST to RETURN_PICKUP")
        void transitionsReturnRequestToReturnPickup() {
            testOrder.setStatus(OrderStatus.RETURN_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.RETURN_PICKUP);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions REFUND_REQUEST to RETURN_PICKUP")
        void transitionsRefundRequestToReturnPickup() {
            testOrder.setStatus(OrderStatus.REFUND_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.RETURN_PICKUP);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions REPLACE_REQUEST to REPLACE_PICKUP")
        void transitionsReplaceRequestToReplacePickup() {
            testOrder.setStatus(OrderStatus.REPLACE_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.REPLACE_PICKUP);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions RETURN_PICKUP to RETURNED")
        void transitionsReturnPickupToReturned() {
            testOrder.setStatus(OrderStatus.RETURN_PICKUP);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.RETURNED);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions RETURN_PICKUP to REFUNDED")
        void transitionsReturnPickupToRefunded() {
            testOrder.setStatus(OrderStatus.RETURN_PICKUP);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.REFUNDED);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("transitions REPLACE_PICKUP to REPLACED")
        void transitionsReplacePickupToReplaced() {
            testOrder.setStatus(OrderStatus.REPLACE_PICKUP);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.REPLACED);

            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("rejects PLACED to RETURN_REQUEST")
        void rejectsPlacedToReturnRequest() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.RETURN_REQUEST))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects PLACED to REFUND_REQUEST")
        void rejectsPlacedToRefundRequest() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.REFUND_REQUEST))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects CONFIRMED to CANCELLED")
        void rejectsConfirmedToCancelled() {
            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CANCELLED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects transition from CANCELLED (terminal state)")
        void rejectsTransitionFromCancelled() {
            testOrder.setStatus(OrderStatus.CANCELLED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.PLACED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects transition from RETURNED (terminal state)")
        void rejectsTransitionFromReturned() {
            testOrder.setStatus(OrderStatus.RETURNED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects transition from REFUNDED (terminal state)")
        void rejectsTransitionFromRefunded() {
            testOrder.setStatus(OrderStatus.REFUNDED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects transition from REPLACED (terminal state)")
        void rejectsTransitionFromReplaced() {
            testOrder.setStatus(OrderStatus.REPLACED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        // ─── Same-status transition ────────────────────────────────────

        @Test
        @DisplayName("rejects same-status transition PLACED to PLACED")
        void rejectsSameStatusTransition() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.PLACED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        // ─── Order not found ───────────────────────────────────────────

        @Test
        @DisplayName("throws OrderNotFoundException when order does not exist")
        void throwsWhenOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED))
                    .isInstanceOf(OrderNotFoundException.class);

            verify(orderRepository, never()).save(any());
        }

        // ─── Invalid cross-flow transitions ─────────────────────────────

        @Test
        @DisplayName("rejects CONFIRMED to CANCEL_REQUEST")
        void rejectsConfirmedToCancelRequest() {
            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CANCEL_REQUEST))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects SHIPPED to CANCEL_REQUEST")
        void rejectsShippedToCancelRequest() {
            testOrder.setStatus(OrderStatus.SHIPPED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CANCEL_REQUEST))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects CONFIRMED to RETURN_REQUEST")
        void rejectsConfirmedToReturnRequest() {
            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.RETURN_REQUEST))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects SHIPPED to RETURN_REQUEST")
        void rejectsShippedToReturnRequest() {
            testOrder.setStatus(OrderStatus.SHIPPED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.RETURN_REQUEST))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects PLACED to REPLACE_REQUEST")
        void rejectsPlacedToReplaceRequest() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.REPLACE_REQUEST))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects CANCEL_REQUEST to PLACED")
        void rejectsCancelRequestToPlaced() {
            testOrder.setStatus(OrderStatus.CANCEL_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.PLACED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects RETURN_REQUEST to DELIVERED")
        void rejectsReturnRequestToDelivered() {
            testOrder.setStatus(OrderStatus.RETURN_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects REFUND_REQUEST to DELIVERED")
        void rejectsRefundRequestToDelivered() {
            testOrder.setStatus(OrderStatus.REFUND_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects REPLACE_REQUEST to DELIVERED")
        void rejectsReplaceRequestToDelivered() {
            testOrder.setStatus(OrderStatus.REPLACE_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects RETURN_PICKUP to DELIVERED")
        void rejectsReturnPickupToDelivered() {
            testOrder.setStatus(OrderStatus.RETURN_PICKUP);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects RETURN_PICKUP to REPLACED")
        void rejectsReturnPickupToReplaced() {
            testOrder.setStatus(OrderStatus.RETURN_PICKUP);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.REPLACED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects REPLACE_PICKUP to RETURNED")
        void rejectsReplacePickupToReturned() {
            testOrder.setStatus(OrderStatus.REPLACE_PICKUP);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.RETURNED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects REPLACE_PICKUP to REFUNDED")
        void rejectsReplacePickupToRefunded() {
            testOrder.setStatus(OrderStatus.REPLACE_PICKUP);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.REFUNDED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects RETURN_REQUEST to CANCELLED")
        void rejectsReturnRequestToCancelled() {
            testOrder.setStatus(OrderStatus.RETURN_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CANCELLED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("rejects REFUND_REQUEST to CANCELLED")
        void rejectsRefundRequestToCancelled() {
            testOrder.setStatus(OrderStatus.REFUND_REQUEST);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CANCELLED))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        // ─── Full lifecycle flow (end-to-end through multiple hops) ────

        @Test
        @DisplayName("full cancel flow — PLACED → CANCEL_REQUEST → CANCELLED")
        void fullCancelFlow() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.CANCEL_REQUEST);
            verify(orderRepository).save(testOrder);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCEL_REQUEST);

            orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);
            verify(orderRepository, times(2)).save(testOrder);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("full return flow — PLACED → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED → RETURN_REQUEST → RETURN_PICKUP → RETURNED")
        void fullReturnFlow() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            OrderStatus[] flow = {
                    OrderStatus.CONFIRMED,
                    OrderStatus.SHIPPED,
                    OrderStatus.OUT_FOR_DELIVERY,
                    OrderStatus.DELIVERED,
                    OrderStatus.RETURN_REQUEST,
                    OrderStatus.RETURN_PICKUP,
                    OrderStatus.RETURNED
            };

            for (OrderStatus next : flow) {
                orderService.updateOrderStatus(1L, next);
            }

            verify(orderRepository, times(flow.length)).save(testOrder);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.RETURNED);
        }

        @Test
        @DisplayName("full refund flow — PLACED → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED → REFUND_REQUEST → RETURN_PICKUP → REFUNDED")
        void fullRefundFlow() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            OrderStatus[] flow = {
                    OrderStatus.CONFIRMED,
                    OrderStatus.SHIPPED,
                    OrderStatus.OUT_FOR_DELIVERY,
                    OrderStatus.DELIVERED,
                    OrderStatus.REFUND_REQUEST,
                    OrderStatus.RETURN_PICKUP,
                    OrderStatus.REFUNDED
            };

            for (OrderStatus next : flow) {
                orderService.updateOrderStatus(1L, next);
            }

            verify(orderRepository, times(flow.length)).save(testOrder);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        }

        @Test
        @DisplayName("full replacement flow — PLACED → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED → REPLACE_REQUEST → REPLACE_PICKUP → REPLACED")
        void fullReplacementFlow() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            OrderStatus[] flow = {
                    OrderStatus.CONFIRMED,
                    OrderStatus.SHIPPED,
                    OrderStatus.OUT_FOR_DELIVERY,
                    OrderStatus.DELIVERED,
                    OrderStatus.REPLACE_REQUEST,
                    OrderStatus.REPLACE_PICKUP,
                    OrderStatus.REPLACED
            };

            for (OrderStatus next : flow) {
                orderService.updateOrderStatus(1L, next);
            }

            verify(orderRepository, times(flow.length)).save(testOrder);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.REPLACED);
        }

        @Test
        @DisplayName("admin bypass — PLACED → CANCELLED direct (skipping CANCEL_REQUEST)")
        void adminDirectCancel() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any())).thenReturn(testOrder);

            orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

            verify(orderRepository).save(testOrder);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getAllOrders() — filter by new statuses
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAllOrders() — filter by new statuses")
    class GetAllOrdersFilterNewStatuses {

        @Test
        @DisplayName("filters by CANCEL_REQUEST status")
        void filtersByCancelRequest() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findByStatus(eq(OrderStatus.CANCEL_REQUEST), any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(OrderStatus.CANCEL_REQUEST, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findByStatus(eq(OrderStatus.CANCEL_REQUEST), any(Pageable.class));
        }

        @Test
        @DisplayName("filters by RETURN_REQUEST status")
        void filtersByReturnRequest() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findByStatus(eq(OrderStatus.RETURN_REQUEST), any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(OrderStatus.RETURN_REQUEST, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findByStatus(eq(OrderStatus.RETURN_REQUEST), any(Pageable.class));
        }

        @Test
        @DisplayName("filters by REFUND_REQUEST status")
        void filtersByRefundRequest() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findByStatus(eq(OrderStatus.REFUND_REQUEST), any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(OrderStatus.REFUND_REQUEST, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findByStatus(eq(OrderStatus.REFUND_REQUEST), any(Pageable.class));
        }

        @Test
        @DisplayName("filters by RETURN_PICKUP status")
        void filtersByReturnPickup() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findByStatus(eq(OrderStatus.RETURN_PICKUP), any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(OrderStatus.RETURN_PICKUP, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findByStatus(eq(OrderStatus.RETURN_PICKUP), any(Pageable.class));
        }

        @Test
        @DisplayName("filters by REFUNDED status")
        void filtersByRefunded() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findByStatus(eq(OrderStatus.REFUNDED), any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(OrderStatus.REFUNDED, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findByStatus(eq(OrderStatus.REFUNDED), any(Pageable.class));
        }

        @Test
        @DisplayName("filters by REPLACE_REQUEST status")
        void filtersByReplaceRequest() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findByStatus(eq(OrderStatus.REPLACE_REQUEST), any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(OrderStatus.REPLACE_REQUEST, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findByStatus(eq(OrderStatus.REPLACE_REQUEST), any(Pageable.class));
        }

        @Test
        @DisplayName("filters by REPLACE_PICKUP status")
        void filtersByReplacePickup() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findByStatus(eq(OrderStatus.REPLACE_PICKUP), any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(OrderStatus.REPLACE_PICKUP, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findByStatus(eq(OrderStatus.REPLACE_PICKUP), any(Pageable.class));
        }

        @Test
        @DisplayName("filters by REPLACED status")
        void filtersByReplaced() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findByStatus(eq(OrderStatus.REPLACED), any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(OrderStatus.REPLACED, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findByStatus(eq(OrderStatus.REPLACED), any(Pageable.class));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getAllOrders() — null status returns all
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAllOrders() — null status")
    class GetAllOrdersNullStatus {

        @Test
        @DisplayName("null status queries findAll instead of findByStatus")
        void nullStatusUsesFindAll() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Order> page = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<AdminOrderResponse> result = orderService.getAllOrders(null, 0, 20);

            assertThat(result.content()).hasSize(1);
            verify(orderRepository).findAll(any(Pageable.class));
            verify(orderRepository, never()).findByStatus(any(), any(Pageable.class));
        }
    }
}