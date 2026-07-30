package com.pkmprojects.shoppiq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Configuration properties for checkout business rules and fee calculations.
 *
 * <p>This class binds to the {@code app.checkout.*} prefix in
 * {@code application.yaml} and defines the monetary values used during the
 * checkout flow. It includes the surcharge for express (one-day) delivery
 * and the additional fee for Cash-on-Delivery (COD) payments. These values
 * are added to the order total during checkout and are displayed to the
 * customer before they confirm their purchase.</p>
 *
 * <p>Architecturally, these properties are consumed by the checkout service
 * to calculate the final order total. By externalizing these values, the
 * business team can adjust delivery fees and COD surcharges without code
 * changes or redeployment. The defaults are set to reasonable values that
 * can be overridden per environment (e.g., free delivery in promotions).</p>
 *
 * @author Prabhat Kumar Mishra
 * @since 1.4.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.checkout")
public class CheckoutProperties {

    /**
     * Additional charge applied when a customer selects
     * express (one-day) delivery.
     *
     * <p>Default value: {@code 7.50}
     */
    private BigDecimal expressDeliveryCharge = new BigDecimal("7.50");

    /**
     * Additional charge applied to orders paid using
     * Cash-on-Delivery (COD).
     *
     * <p>Default value: {@code 5.00}
     */
    private BigDecimal codSurcharge = new BigDecimal("5.00");

}