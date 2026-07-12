package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.email.EmailService;
import com.pkmprojects.shoppiq.email.EmailType;
import com.pkmprojects.shoppiq.email.dto.EmailMessage;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.OrderAddressSnapshot;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending order lifecycle emails.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEmailService {

    private final EmailService emailService;

    /**
     * Sends an order status update email to the customer.
     *
     * @param order     the order
     * @param newStatus the new status
     */
    public void sendOrderStatusEmail(Order order, OrderStatus newStatus) {
        User user = order.getUser();
        String subject = getStatusSubject(newStatus, order.getId());
        String statusMessage = getStatusMessage(newStatus);

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", user.getName());
        vars.put("orderId", order.getId());
        vars.put("orderTitle", subject);
        vars.put("orderMessage", statusMessage);
        vars.put("orderStatus", newStatus.name().replace("_", " "));
        vars.put("orderTotal", "$" + order.getGrandTotal());
        vars.put("isDelivered", newStatus == OrderStatus.DELIVERED);

        if (newStatus == OrderStatus.DELIVERED && order.getShippingAddress() != null) {
            vars.put("deliveryAddress", formatAddress(order.getShippingAddress()));
        }

        emailService.sendEmail(EmailMessage.builder()
                .to(user.getEmail())
                .subject(subject)
                .templateName("order-update")
                .emailType(EmailType.ORDER_UPDATE)
                .userId(user.getId())
                .variables(vars)
                .build());

        log.debug("Order status email sent: orderId={}, status={}, userId={}", order.getId(), newStatus, user.getId());
    }

    private String getStatusSubject(OrderStatus status, Long orderId) {
        return switch (status) {
            case PLACED -> "Order #%d Placed Successfully".formatted(orderId);
            case CONFIRMED -> "Order #%d Confirmed".formatted(orderId);
            case SHIPPED -> "Order #%d Has Been Shipped".formatted(orderId);
            case OUT_FOR_DELIVERY -> "Order #%d Is Out for Delivery".formatted(orderId);
            case DELIVERED -> "Order #%d Has Been Delivered".formatted(orderId);
            case CANCELLED -> "Order #%d Has Been Cancelled".formatted(orderId);
            case RETURNED -> "Order #%d Return Confirmed".formatted(orderId);
            case CANCEL_REQUEST -> "Order #%d Cancellation Requested".formatted(orderId);
            case RETURN_REQUEST -> "Order #%d Return Requested".formatted(orderId);
            case REFUND_REQUEST -> "Order #%d Refund Requested".formatted(orderId);
            case REFUNDED -> "Order #%d Refund Processed".formatted(orderId);
            case RETURN_PICKUP -> "Order #%d Return Pickup Scheduled".formatted(orderId);
            case REPLACE_PICKUP -> "Order #%d Replacement Pickup Scheduled".formatted(orderId);
            case REPLACE_REQUEST -> "Order #%d Replacement Requested".formatted(orderId);
            case REPLACED -> "Order #%d Replacement Completed".formatted(orderId);
        };
    }

    private String getStatusMessage(OrderStatus status) {
        return switch (status) {
            case PLACED -> "Your order has been placed successfully. We'll start processing it shortly.";
            case CONFIRMED -> "Your order has been confirmed and is being prepared.";
            case SHIPPED -> "Great news! Your order has been shipped and is on its way to you.";
            case OUT_FOR_DELIVERY -> "Your order is out for delivery and will arrive soon.";
            case DELIVERED -> "Your order has been delivered. We hope you enjoy your purchase!";
            case CANCELLED -> "Your order has been cancelled. If you have any questions, please contact support.";
            case RETURNED -> "Your return has been confirmed. A refund will be processed shortly.";
            case CANCEL_REQUEST -> "Your cancellation request has been received and is being reviewed.";
            case RETURN_REQUEST -> "Your return request has been received and is being processed.";
            case REFUND_REQUEST -> "Your refund request has been received and is being processed.";
            case REFUNDED -> "Your refund has been processed. The amount will be credited to your account shortly.";
            case RETURN_PICKUP -> "A pickup has been scheduled for your return item(s). Please have them ready.";
            case REPLACE_PICKUP -> "A pickup has been scheduled for your replacement item(s). Please have them ready.";
            case REPLACE_REQUEST -> "Your replacement request has been received and is being processed.";
            case REPLACED -> "Your replacement has been completed. The new item(s) will be shipped shortly.";
        };
    }

    private String formatAddress(OrderAddressSnapshot addr) {
        StringBuilder sb = new StringBuilder();
        if (addr.getLine1() != null && !addr.getLine1().isBlank()) sb.append(addr.getLine1());
        if (addr.getLine2() != null && !addr.getLine2().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(addr.getLine2());
        }
        if (addr.getCity() != null && !addr.getCity().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(addr.getCity());
        }
        if (addr.getState() != null && !addr.getState().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(addr.getState());
        }
        if (addr.getPostalCode() != null && !addr.getPostalCode().isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(addr.getPostalCode());
        }
        if (addr.getCountry() != null && !addr.getCountry().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(addr.getCountry());
        }
        return sb.toString();
    }
}
