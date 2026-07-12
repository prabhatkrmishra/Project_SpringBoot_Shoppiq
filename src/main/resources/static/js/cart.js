/* ─── Cart Operations (Backend API) ─────────────────────
 *  All cart operations go through the REST API.
 *  Uses window.API with credentials: "include" for JWT cookie auth.
 * ──────────────────────────────────────────────────────── */
(function () {
    'use strict';

    /* Update the header cart badge count from the server cart */
    window.updateCartBadge = function () {
        API.get('/user/cart/get')
            .then(function (cart) {
                var total = cart && cart.totalItems != null ? cart.totalItems : 0;
                var badge = document.querySelector('.badge-count');
                if (badge) {
                    badge.textContent = total;
                    badge.style.display = total > 0 ? 'inline-flex' : 'none';
                }
            })
            .catch(function () {
                /* silently ignore — user may not be logged in */
            });
    };

    /* Refresh badge on page load */
    document.addEventListener('DOMContentLoaded', function () {
        window.updateCartBadge();
    });
})();
