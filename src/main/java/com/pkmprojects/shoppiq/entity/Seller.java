package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a seller on the marketplace platform.
 *
 * <p>
 * Each seller is linked to a single {@link User} via a one-to-one
 * relationship. A seller may own multiple {@link Item items} and
 * has one {@link Store}.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores seller identity and contact details.</li>
 *     <li>Tracks verification and operational status independently.</li>
 *     <li>Holds a flat commission rate for admin reporting.</li>
 *     <li>Stores an aggregate rating populated by a background job.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} for id, version, and timestamps.</li>
 *     <li>{@code verificationStatus} and {@code sellerStatus} are independent
 *         dimensions — a seller is {@code ACTIVE} only after being
 *         {@code APPROVED}.
 *     </li>
 *     <li>The {@code rating} field is an aggregate populated asynchronously,
 *         not computed live.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(name = "sellers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Seller extends AuditableEntity {

    /**
     * The user account that owns this seller profile.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_sellers_user")
    )
    private User user;

    /**
     * Registered business name.
     */
    @NotBlank
    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    /**
     * Business email address (validated independently of {@link User#getEmail()}).
     */
    @NotBlank
    @Email
    @Column(name = "business_email", nullable = false, length = 255)
    private String businessEmail;

    /**
     * Contact phone number.
     */
    @NotBlank
    @Column(nullable = false, length = 15)
    private String phone;

    /**
     * GST registration number (optional at schema level; validated at business-logic layer).
     */
    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    /**
     * Permanent Account Number (India context).
     */
    @NotBlank
    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    /**
     * Business address.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "business_address_id",
            foreignKey = @ForeignKey(name = "fk_sellers_business_address")
    )
    private Address businessAddress;

    /**
     * Verification status (PENDING, APPROVED, REJECTED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus;

    /**
     * Operational status (ACTIVE, SUSPENDED, INACTIVE).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "seller_status", nullable = false, length = 20)
    private SellerStatus sellerStatus;

    /**
     * Flat commission rate percentage (e.g. 5.00 = 5%).
     *
     * <p>Used for admin commission-earned reporting. Payout logic
     * is future-ready and not part of Phase 1.</p>
     */
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    /**
     * Aggregate seller rating.
     *
     * <p>Populated via a scheduled job or on-review-write, not computed live.</p>
     */
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    /**
     * Timestamp when the seller profile was first created.
     */
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
