package com.pkmprojects.shoppiq.entity.seller;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a seller on the Shoppiq marketplace platform.
 *
 * <p>Each seller is linked to a single {@link User} via a one-to-one
 * relationship and may own multiple {@link Item items} listed in the
 * catalog. A seller has exactly one {@link Store} that serves as their
 * storefront. Tracks verification status ({@link VerificationStatus})
 * and operational status ({@link SellerStatus}) as independent
 * dimensions, allowing administrators to manage seller onboarding and
 * account health separately.</p>
 *
 * <p>Holds a flat commission rate for admin reporting and an aggregate
 * rating populated asynchronously by a background job that averages
 * ratings from all approved reviews across the seller's products. The
 * seller profile includes business registration details (GST, PAN)
 * required for compliance and tax reporting in the Indian marketplace
 * context.</p>
 *
 * @author prabhatkrmishra
 * @see User
 * @see Store
 * @see Item
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
     *
     * <p>One-to-one relationship linking the seller to their platform
     * login and authentication credentials. The {@code user_id} column
     * carries a unique constraint to ensure each user can only have one
     * seller profile. Lazily loaded to avoid unnecessary joins when
     * accessing seller details independently.</p>
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_sellers_user")
    )
    private User user;

    /**
     * Registered business name of the seller, displayed on product
     * listings, invoices, and seller profile pages.
     *
     * <p>Required field with a maximum length of 255 characters. This
     * is the primary public-facing identifier for the seller and
     * appears in search results, product cards, and customer-facing
     * communications. Must be provided during seller registration.</p>
     */
    @NotBlank
    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    /**
     * Business email address for seller-specific communications,
     * order notifications, and platform correspondence.
     *
     * <p>Required field validated against RFC 5322 email format. This
     * email is independent of the linked {@link User} email and is
     * used exclusively for business communications. Displayed in the
     * seller directory and used for administrative outreach.</p>
     */
    @NotBlank
    @Email
    @Column(name = "business_email", nullable = false, length = 255)
    private String businessEmail;

    /**
     * Contact phone number for seller-related communications and
     * platform support.
     *
     * <p>Required field with a maximum length of 15 characters to
     * accommodate international phone number formats. Used by the
     * platform for urgent order-related notifications and by
     * customers for seller contact purposes where applicable.</p>
     */
    @NotBlank
    @Column(nullable = false, length = 15)
    private String phone;

    /**
     * GST (Goods and Services Tax) registration number for the seller's
     * business entity.
     *
     * <p>Optional at the schema level; validated at the business-logic
     * layer during seller onboarding. Required for Indian marketplace
     * compliance. Maximum length of 20 characters. Used for tax invoice
     * generation and GST filing purposes.</p>
     */
    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    /**
     * Permanent Account Number (PAN) for the seller's business entity,
     * required for Indian tax compliance.
     *
     * <p>Required field with a fixed length of 10 characters. Used for
     * TDS (Tax Deducted at Source) compliance and financial reporting.
     * Validated against the Indian PAN format during seller registration
     * and onboarding workflows.</p>
     */
    @NotBlank
    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    /**
     * Business address for the seller's registered office or primary
     * operating location.
     *
     * <p>Optional relationship. When present, used for tax calculation,
     * regulatory compliance, and business verification purposes. The
     * address is lazily loaded to avoid unnecessary joins. This address
     * is separate from any store pickup addresses managed through the
     * {@link Store} entity.</p>
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "business_address_id",
            foreignKey = @ForeignKey(name = "fk_sellers_business_address")
    )
    private Address businessAddress;

    /**
     * Verification status tracking the seller's identity and document
     * verification progress.
     *
     * <p>Stored as a string enum with a maximum length of 20 characters.
     * Transitions from {@code PENDING} during onboarding to
     * {@code APPROVED} or {@code REJECTED} based on admin review of
     * submitted documents. Only sellers with {@code APPROVED} status
     * can list products and receive orders.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus;

    /**
     * Operational status controlling the seller's ability to manage
     * products and fulfill orders.
     *
     * <p>Stored as a string enum with a maximum length of 20 characters.
     * Independent from verification status. Transitions between
     * {@code ACTIVE}, {@code SUSPENDED}, and {@code INACTIVE} based
     * on admin actions or policy violations. Only sellers with
     * {@code ACTIVE} status can receive new orders.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "seller_status", nullable = false, length = 20)
    private SellerStatus sellerStatus;

    /**
     * Flat commission rate percentage applied to each of the seller's
     * sales for platform revenue reporting.
     *
     * <p>Optional field with a precision of 5 digits total and 2 decimal
     * places (e.g. 5.00 represents a 5% commission). Used for admin
     * commission-earned reporting and seller payout calculations.
     * Payout logic is future-ready and not part of Phase 1
     * implementation.</p>
     */
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    /**
     * Aggregate seller rating computed from approved reviews across
     * all of the seller's products.
     *
     * <p>Populated via a scheduled background job or on-review-write
     * event, not computed live to avoid performance overhead. Precision
     * is 3 digits total with 2 decimal places (e.g. 4.75). Displayed
     * on the seller's storefront and in search results to help
     * customers assess seller quality.</p>
     */
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    /**
     * Timestamp when the seller profile was first created during the
     * onboarding process.
     *
     * <p>Set to the current UTC time at the moment of seller registration.
     * Used for seller tenure calculations, onboarding analytics, and
     * displaying "Seller since YYYY" on the storefront. Stored as an
     * {@link Instant} for timezone independence.</p>
     */
    @Column(name = "joined_at")
    private Instant joinedAt;
}
