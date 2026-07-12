/* ─── Pagination Component ─────────────────────────────────
 *  Reusable pagination UI for all list pages.
 *
 *  Usage:
 *    Pagination.render('container-id', pageResponse, onPageChange, opts);
 *
 *  pageResponse must have: { content, page, size, totalElements, totalPages, first, last }
 *
 *  Options:
 *    - siblingCount: pages to show around current (default: 1)
 *    - pageSizes: array of allowed page sizes (default: [10, 20, 50, 100])
 *    - showPageSize: show page size selector (default: true)
 *    - showJump: show page jump input when totalPages > 10 (default: true)
 *    - onPageSizeChange: callback(newSize) when page size changes
 * ──────────────────────────────────────────────────────── */

window.Pagination = (function () {

    /**
     * Renders pagination controls into the given container.
     *
     * @param {string} containerId - DOM element ID to render into
     * @param {object} data - PageResponse from the API
     * @param {function} onPageChange - callback(newPage) when user clicks a page
     * @param {object} [opts] - optional settings
     * @param {number} [opts.siblingCount=1] - pages to show around current
     * @param {number[]} [opts.pageSizes=[10,20,50,100]] - available page sizes
     * @param {boolean} [opts.showPageSize=true] - show page size selector
     * @param {boolean} [opts.showJump=true] - show page jump input for many pages
     * @param {function} [opts.onPageSizeChange] - callback(newSize) when page size changes
     */
    function render(containerId, data, onPageChange, opts) {
        var container = document.getElementById(containerId);
        if (!container) return;
        container.innerHTML = '';

        if (!data || data.totalElements == null || data.totalPages <= 1) return;

        var siblingCount = (opts && opts.siblingCount != null) ? opts.siblingCount : 1;
        var pageSizes = (opts && opts.pageSizes) ? opts.pageSizes : [10, 20, 50, 100];
        var showPageSize = !(opts && opts.showPageSize === false);
        var showJump = !(opts && opts.showJump === false);
        var onPageSizeChange = (opts && opts.onPageSizeChange) ? opts.onPageSizeChange : null;
        var currentPage = data.page;
        var totalPages = data.totalPages;
        var currentSize = data.size;

        var wrapper = document.createElement('div');
        wrapper.className = 'pagination';

        // Info text (left side)
        var start = currentPage * currentSize + 1;
        var end = Math.min((currentPage + 1) * currentSize, data.totalElements);
        var info = document.createElement('div');
        info.className = 'pagination-info';
        info.setAttribute('role', 'status');
        info.setAttribute('aria-live', 'polite');
        info.innerHTML = '<span class="pagination-range">' + start + '&ndash;' + end + '</span> of <strong>' + data.totalElements + '</strong>';
        wrapper.appendChild(info);

        // Center: page buttons
        var buttons = document.createElement('div');
        buttons.className = 'pagination-buttons';
        buttons.setAttribute('role', 'navigation');
        buttons.setAttribute('aria-label', 'Pagination');

        // Prev button
        var prevBtn = createButton('‹', 'Previous page', currentPage > 0, function () {
            onPageChange(currentPage - 1);
        });
        buttons.appendChild(prevBtn);

        // Page number buttons
        var pages = generatePageNumbers(currentPage, totalPages, siblingCount);
        for (var i = 0; i < pages.length; i++) {
            if (pages[i] === '...') {
                var ellipsis = document.createElement('span');
                ellipsis.className = 'pagination-ellipsis';
                ellipsis.setAttribute('aria-hidden', 'true');
                ellipsis.textContent = '...';
                buttons.appendChild(ellipsis);
            } else {
                (function (pageNum) {
                    var isActive = pageNum === currentPage;
                    var btn = createButton(String(pageNum + 1), 'Page ' + (pageNum + 1) + (isActive ? ' (current)' : ''), true, function () {
                        onPageChange(pageNum);
                    });
                    if (isActive) {
                        btn.classList.add('active');
                        btn.setAttribute('aria-current', 'page');
                    }
                    buttons.appendChild(btn);
                })(pages[i]);
            }
        }

        // Next button
        var nextBtn = createButton('›', 'Next page', currentPage < totalPages - 1, function () {
            onPageChange(currentPage + 1);
        });
        buttons.appendChild(nextBtn);

        wrapper.appendChild(buttons);

        // Right side: page size selector + jump input
        var controls = document.createElement('div');
        controls.className = 'pagination-controls';

        if (showPageSize) {
            var sizeSelect = createPageSizeSelect(currentSize, pageSizes, onPageSizeChange);
            controls.appendChild(sizeSelect);
        }

        if (showJump && totalPages > 10) {
            var jumpInput = createPageJumpInput(currentPage, totalPages, onPageChange);
            controls.appendChild(jumpInput);
        }

        wrapper.appendChild(controls);
        container.appendChild(wrapper);
    }

    function createButton(text, ariaLabel, enabled, onClick) {
        var btn = document.createElement('button');
        btn.className = 'pagination-btn';
        btn.textContent = text;
        btn.type = 'button';
        btn.setAttribute('aria-label', ariaLabel);
        if (!enabled) {
            btn.disabled = true;
            btn.classList.add('disabled');
            btn.setAttribute('aria-disabled', 'true');
        } else {
            btn.addEventListener('click', onClick);
        }
        return btn;
    }

    function createPageSizeSelect(currentSize, pageSizes, onChange) {
        var wrapper = document.createElement('div');
        wrapper.className = 'pagination-size';

        var label = document.createElement('label');
        label.className = 'pagination-size-label';
        label.htmlFor = 'pagination-page-size';
        label.textContent = 'Show:';
        wrapper.appendChild(label);

        var select = document.createElement('select');
        select.id = 'pagination-page-size';
        select.className = 'pagination-size-select';
        select.setAttribute('aria-label', 'Items per page');

        for (var i = 0; i < pageSizes.length; i++) {
            var opt = document.createElement('option');
            opt.value = pageSizes[i];
            opt.textContent = pageSizes[i];
            opt.selected = pageSizes[i] === currentSize;
            select.appendChild(opt);
        }

        select.addEventListener('change', function () {
            var newSize = parseInt(this.value, 10);
            if (onChange) onChange(newSize);
        });

        wrapper.appendChild(select);
        return wrapper;
    }

    function createPageJumpInput(currentPage, totalPages, onPageChange) {
        var wrapper = document.createElement('div');
        wrapper.className = 'pagination-jump';

        var label = document.createElement('label');
        label.className = 'pagination-jump-label';
        label.htmlFor = 'pagination-page-jump';
        label.textContent = 'Go to page:';
        wrapper.appendChild(label);

        var input = document.createElement('input');
        input.type = 'number';
        input.id = 'pagination-page-jump';
        input.className = 'pagination-jump-input';
        input.min = 1;
        input.max = totalPages;
        input.value = currentPage + 1;
        input.setAttribute('aria-label', 'Page number (1-' + totalPages + ')');
        input.setAttribute('aria-describedby', 'pagination-jump-hint');

        var hint = document.createElement('span');
        hint.id = 'pagination-jump-hint';
        hint.className = 'pagination-jump-hint sr-only';
        hint.textContent = 'Enter a page number between 1 and ' + totalPages;
        wrapper.appendChild(hint);

        var submitBtn = document.createElement('button');
        submitBtn.type = 'button';
        submitBtn.className = 'pagination-jump-btn';
        submitBtn.textContent = 'Go';
        submitBtn.setAttribute('aria-label', 'Go to page');

        var submitJump = function () {
            var page = parseInt(input.value, 10);
            if (!isNaN(page) && page >= 1 && page <= totalPages) {
                onPageChange(page - 1);
            }
        };

        submitBtn.addEventListener('click', submitJump);
        input.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') submitJump();
        });

        wrapper.appendChild(input);
        wrapper.appendChild(submitBtn);
        return wrapper;
    }

    function generatePageNumbers(current, total, siblingCount) {
        var pages = [];
        var left = Math.max(current - siblingCount, 0);
        var right = Math.min(current + siblingCount, total - 1);

        if (left > 0) {
            pages.push(0);
            if (left > 1) pages.push('...');
        }

        for (var i = left; i <= right; i++) {
            pages.push(i);
        }

        if (right < total - 1) {
            if (right < total - 2) pages.push('...');
            pages.push(total - 1);
        }

        return pages;
    }

    return {render: render};

})();
