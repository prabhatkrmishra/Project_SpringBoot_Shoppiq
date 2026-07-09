package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when an attempt is made to put a product on sale
 * that is already on sale.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class ProductAlreadyOnSaleException extends InvalidOperationException {

    private ProductAlreadyOnSaleException(String detail) {
        super(ErrorCode.ITEM_ALREADY_ON_SALE, detail);
    }

    public static ProductAlreadyOnSaleException forItem(String itemName) {
        return new ProductAlreadyOnSaleException(
                "Product '%s' is already on sale."
                        .formatted(itemName)
        );
    }
}
