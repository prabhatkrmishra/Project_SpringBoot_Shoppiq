/* ─── Cart Badge (Navbar) ──────────────────────────────
 *  Fetches the server cart and updates every .badge-count
 *  element in the navbar (desktop + mobile) with the
 *  current item count.
 * ──────────────────────────────────────────────────────── */
(function () {
    'use strict';

    /* Update all header cart badge elements from the server cart */
    window.updateCartBadge = function () {
        API.get('/user/cart/get')
            .then(function (cart) {
                var total = cart && cart.totalItems != null ? cart.totalItems : 0;
                var badges = document.querySelectorAll('.badge-count');
                badges.forEach(function (badge) {
                    badge.textContent = total;
                    badge.style.display = total > 0 ? 'inline-flex' : 'none';
                });
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
