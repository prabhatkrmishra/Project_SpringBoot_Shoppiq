package com.pkmprojects.shoppiq.entity.seller;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.enums.StoreStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * <strong>Spring Boot Concept:</strong> Represents a seller's store on the marketplace.
 *
 * <p>
 * Each seller has exactly one store. The store is auto-created in
 * {@code DRAFT} status when a seller is approved by admin, and the
 * seller must complete the profile before publishing.
 * </p>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} for id, version, and timestamps.</li>
 *     <li>One-to-one with {@link Seller} — multiple stores per seller
 *         is a future enhancement.</li>
 *     <li>When a seller is suspended, the store status should transition
 *         to {@code SUSPENDED} via service-layer cascade (not a DB trigger).</li>
 * </ul>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>{@code @OneToOne} with Seller</strong> — The FK is on this
 *         table ({@code seller_id}), making Store the owning side. The
 *         {@code unique = true} constraint enforces one store per seller.</li>
 *     <li><strong>Store as a profile entity</strong> — Store extends Seller
 *         with storefront-specific fields (name, slug, description, logo,
 *         banner) while the Seller entity handles identity, verification,
 *         and financials. This separation of concerns is a common
 *         architecture pattern.</li>
 *     <li><strong>{@link StoreStatus} enum</strong> — Three states
 *         (DRAFT/PUBLISHED/SUSPENDED) model the store's lifecycle. DRAFT
 *         allows sellers to set up before going live.</li>
 *     <li><strong>Slug for URL-friendly routing</strong> — The {@code slug}
 *         is unique and can be used in RESTful URLs (e.g.,
 *         {@code /store/my-store-name}) for SEO-friendly navigation.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Entity
@Table(name = "stores")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Store extends AuditableEntity {

    /**
     * The seller who owns this store.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "seller_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_stores_seller")
    )
    private Seller seller;

    /**
     * Display name of the store.
     */
    @NotBlank
    @Column(name = "store_name", nullable = false, length = 255)
    private String storeName;

    /**
     * URL-friendly unique identifier for the store.
     */
    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    /**
     * Store description.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * URL or key for the store logo image.
     */
    @Column(length = 500)
    private String logo;

    /**
     * URL or key for the store banner image.
     */
    @Column(length = 500)
    private String banner;

    /**
     * Publishing status of the store.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoreStatus status;
}
