/* ─── Product Card ─────────────────────────────────────────
 *  Shared product card renderer used across all shop pages.
 *  Depends on: api.js (window.escapeHtml, window.API, window.formatCurrency, window.initIcons)
 * ────────────────────────────────────────────────────────── */

window.ProductCard = (function () {
    'use strict';

    var DEFAULT_IMG = 'https://plus.unsplash.com/premium_photo-1683746792239-6ce8cdd3ac78?q=80&w=687&fit=crop';

    function escapeAttr(str) {
        return (str || '').replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function truncateWords(str, max) {
        if (!str) return '';
        var w = str.trim().split(/\s+/);
        return w.length <= max ? str : w.slice(0, max).join(' ') + '\u2026';
    }

    function calcSalePrice(price, discount) {
        return (parseFloat(price) * (1 - parseFloat(discount) / 100)).toFixed(2);
    }

    function stockLevel(qty) {
        if (qty == null || isNaN(qty)) return {cls: 'badge-neutral', text: 'Stock Unknown'};
        if (qty > 10) return {cls: 'badge-success', text: 'In Stock'};
        if (qty > 0) return {cls: 'badge-warning', text: 'Low Stock'};
        return {cls: 'badge-danger', text: 'Out of Stock'};
    }

    function fmt(n) {
        if (typeof window.formatCurrency === 'function') return window.formatCurrency(n);
        return '$' + parseFloat(n || 0).toFixed(2);
    }

    function esc(s) {
        if (typeof window.escapeHtml === 'function') return window.escapeHtml(s);
        if (!s) return '';
        var d = document.createElement('div');
        d.appendChild(document.createTextNode(s));
        return d.innerHTML;
    }

    /* ── Core renderer ────────────────────────────────── */
    function render(item, opts) {
        opts = opts || {};
        var imgUrl = item.imageUrl || DEFAULT_IMG;
        var hasDiscount = item.discountPercentage && parseFloat(item.discountPercentage) > 0;
        var showStock = opts.showStock !== false;
        var showCategory = opts.showCategory !== false;
        var alwaysSale = opts.alwaysShowSaleBadge || false;
        var saleBadgeClass = opts.saleBadgeClass || 'badge-success';

        /* Price HTML */
        var priceHtml = '<span class="price-new">' + fmt(item.price) + '</span>';
        if (hasDiscount) {
            var sp = calcSalePrice(item.price, item.discountPercentage);
            priceHtml =
                '<span class="price-old">' + fmt(item.price) + '</span>' +
                '<span class="price-new">' + fmt(sp) + '</span>' +
                '<span class="price-discount">' + esc(String(item.discountPercentage)) + '% Off</span>';
        }

        /* Badge */
        var badgeHtml = '';
        if (alwaysSale || hasDiscount) {
            badgeHtml = '<span class="card-badge badge ' + saleBadgeClass + '">Sale</span>';
        } else if (item.isNewArrival) {
            badgeHtml = '<span class="card-badge badge badge-info">New</span>';
        }

        /* Category */
        var categoryHtml = '';
        if (showCategory && item.category && item.category.name) {
            categoryHtml = '<span class="card-category">' + esc(item.category.name) + '</span>';
        }

        /* Stock */
        var stockHtml = '';
        var stockQty = item.stockQuantity != null ? Number(item.stockQuantity) : null;
        if (showStock) {
            var s = stockLevel(stockQty);
            stockHtml = '<div class="card-meta"><span class="badge ' + s.cls + '">' + s.text + '</span></div>';
        }

        /* Action buttons */
        var actionsHtml =
            '<div class="card-actions">' +
            '<button type="button" class="btn card-action-wishlist" title="Add to wishlist" data-item-id="' + escapeAttr(String(item.id)) + '">' +
            '<i data-lucide="heart" class="icon-sm"></i>' +
            '</button>' +
            '<button type="button" class="btn card-action-cart" title="Add to cart" data-item-details-id="' + escapeAttr(String(item.itemDetailsId || '')) + '" data-item-id="' + escapeAttr(String(item.id)) + '"' +
            (stockQty != null && stockQty <= 0 ? ' disabled' : '') + '>' +
            '<i data-lucide="shopping-bag" class="icon-sm"></i>' +
            '</button>' +
            '</div>';

        return '<a href="/item/' + escapeAttr(item.slug) + '" class="card-link">' +
            '<article class="card product-card" data-item-id="' + escapeAttr(String(item.id)) + '">' +
            '<div class="card-image-wrapper">' +
            '<img class="card-image" src="' + escapeAttr(imgUrl) + '" alt="' + escapeAttr(item.name) + '" loading="lazy" width="300" height="300">' +
            badgeHtml +
            actionsHtml +
            '<div class="card-image-overlay"></div>' +
            '</div>' +
            '<div class="card-body">' +
            categoryHtml +
            '<h3 class="card-title">' + esc(item.name) + '</h3>' +
            '<p class="card-text">' + truncateWords(esc(item.description || ''), 12) + '</p>' +
            '<div class="card-price">' + priceHtml + '</div>' +
            stockHtml +
            '</div>' +
            '</article>' +
            '</a>';
    }

    /* ── Render a skeleton placeholder card ───────────── */
    function renderSkeleton() {
        return '<div class="card product-card card-skeleton">' +
            '<div class="card-image-wrapper skeleton-block skeleton-shimmer" style="aspect-ratio:1;"></div>' +
            '<div class="card-body">' +
            '<div class="skeleton-line skeleton-shimmer" style="width:40%;height:10px;margin-bottom:10px;"></div>' +
            '<div class="skeleton-line skeleton-shimmer" style="width:80%;height:14px;margin-bottom:8px;"></div>' +
            '<div class="skeleton-line skeleton-shimmer" style="width:60%;height:10px;margin-bottom:14px;"></div>' +
            '<div class="skeleton-line skeleton-shimmer" style="width:30%;height:18px;margin-bottom:8px;"></div>' +
            '<div class="skeleton-line skeleton-shimmer" style="width:50%;height:10px;"></div>' +
            '</div>' +
            '</div>';
    }

    /* ── Render grid of skeleton cards ────────────────── */
    function showSkeletons(gridEl, count) {
        count = count || 8;
        var html = '';
        for (var i = 0; i < count; i++) html += renderSkeleton();
        gridEl.innerHTML = html;
        gridEl.style.display = '';
    }

    /* ── Render a full grid of cards with stagger ─────── */
    function renderGrid(gridEl, items, opts) {
        opts = opts || {};
        var html = items.map(function (item) {
            return render(item, opts);
        }).join('');
        gridEl.innerHTML = html;
        gridEl.style.display = '';

        /* Stagger entrance animation */
        var cards = gridEl.querySelectorAll('.product-card');
        cards.forEach(function (card, i) {
            card.style.animationDelay = (i * 50) + 'ms';
            card.classList.add('card-animate-in');
        });

        /* Init icons in new nodes */
        if (typeof lucide !== 'undefined') lucide.createIcons({nodes: [gridEl]});

        /* Wire up interactive actions */
        initActions(gridEl);
    }

    /* ── Wishlist + Cart click handlers ───────────────── */
    function initActions(container) {
        /* Wishlist toggle */
        container.querySelectorAll('.card-action-wishlist').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                btn.classList.toggle('active');
                if (btn.classList.contains('active')) {
                    if (typeof showToast === 'function') showToast('Added to wishlist', 'success');
                } else {
                    if (typeof showToast === 'function') showToast('Removed from wishlist', 'info');
                }
            });
        });

        /* Add to cart */
        container.querySelectorAll('.card-action-cart').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                if (btn.disabled) return;
                var detailsId = btn.getAttribute('data-item-details-id');
                if (!detailsId) {
                    if (typeof showToast === 'function') showToast('Could not add to cart', 'error');
                    return;
                }
                btn.classList.add('adding');
                API.post('/user/cart/create', {itemDetailsId: parseInt(detailsId), quantity: 1})
                    .then(function () {
                        if (typeof showToast === 'function') showToast('Added to cart', 'success');
                        if (typeof window.updateCartBadge === 'function') window.updateCartBadge();
                    })
                    .catch(function (err) {
                        if (typeof showToast === 'function') showToast(err.message || 'Could not add to cart', 'error');
                    })
                    .finally(function () {
                        btn.classList.remove('adding');
                    });
            });
        });
    }

    /* ── Public API ───────────────────────────────────── */
    return {
        render: render,
        renderSkeleton: renderSkeleton,
        renderGrid: renderGrid,
        showSkeletons: showSkeletons,
        initActions: initActions
    };
})();
