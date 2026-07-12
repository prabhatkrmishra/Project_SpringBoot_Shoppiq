// ─── Lucide Icons ─────────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
    initScrollReveal();
    initBackToTop();
    initHeroParallax();
    initPageTransitions();
    initFormValidation();
});

// ─── Utility: Debounce ────────────────────────────────
window.debounce = function (fn, delay) {
    var timer;
    return function () {
        var context = this, args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            fn.apply(context, args);
        }, delay);
    };
};

// ─── Mobile Menu ──────────────────────────────────────
document.addEventListener('click', function (e) {
    var toggle = e.target.closest('.menu-toggle');
    var menu = document.querySelector('.mobile-menu');
    if (toggle && menu) {
        var expanded = toggle.getAttribute('aria-expanded') === 'true';
        toggle.setAttribute('aria-expanded', !expanded);
        menu.setAttribute('aria-hidden', expanded);
    }
});

// ─── Toast Notifications ──────────────────────────────
window.showToast = function (message, type) {
    var container = document.getElementById('toast-container');
    if (!container) return;
    var toast = document.createElement('div');
    toast.className = 'toast toast-' + (type || 'info');
    toast.setAttribute('role', 'alert');
    var iconMap = {success: 'check-circle', error: 'x-circle', warning: 'alert-triangle', info: 'info'};
    var iconName = iconMap[type] || 'info';
    toast.innerHTML = '<i data-lucide="' + iconName + '" class="toast-icon"></i><span class="toast-message">' + window.escapeHtml(message) + '</span><button class="toast-close" onclick="this.parentElement.remove()" aria-label="Dismiss">&times;</button>';
    container.appendChild(toast);
    if (typeof lucide !== 'undefined') {
        lucide.createIcons({nodes: [toast]});
    }
    setTimeout(function () {
        toast.classList.add('toast-exit');
        setTimeout(function () {
            toast.remove();
        }, 200);
    }, 4000);
};

// ─── Confirm Modal (replaces native confirm()) ────────
window.showConfirmModal = function (opts) {
    return new Promise(function (resolve) {
        var title = opts.title || 'Confirm';
        var message = opts.message || 'Are you sure?';
        var confirmText = opts.confirmText || 'Confirm';
        var cancelText = opts.cancelText || 'Cancel';
        var danger = opts.danger || false;

        var overlay = document.createElement('div');
        overlay.className = 'modal-overlay active';
        overlay.setAttribute('role', 'dialog');
        overlay.setAttribute('aria-modal', 'true');
        overlay.innerHTML =
            '<div class="modal">' +
            '<div class="modal-header">' +
            '<h2>' + escapeHtml(title) + '</h2>' +
            '</div>' +
            '<div class="modal-body">' +
            '<p>' + escapeHtml(message) + '</p>' +
            '</div>' +
            '<div class="modal-actions">' +
            '<button class="btn btn-secondary confirm-cancel-btn">' + escapeHtml(cancelText) + '</button>' +
            '<button class="btn ' + (danger ? 'btn-danger' : 'btn-primary') + ' confirm-ok-btn">' + escapeHtml(confirmText) + '</button>' +
            '</div>' +
            '</div>';

        document.body.appendChild(overlay);

        function close(result) {
            overlay.classList.remove('active');
            setTimeout(function () {
                overlay.remove();
            }, 200);
            resolve(result);
        }

        overlay.querySelector('.confirm-ok-btn').addEventListener('click', function () {
            close(true);
        });
        overlay.querySelector('.confirm-cancel-btn').addEventListener('click', function () {
            close(false);
        });
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) close(false);
        });
        overlay.querySelector('.confirm-ok-btn').focus();
    });
};

// ─── Alert Modal (replaces native alert()) ────────────
window.showAlertModal = function (opts) {
    return new Promise(function (resolve) {
        var title = opts.title || 'Notice';
        var message = opts.message || '';
        var buttonText = opts.buttonText || 'OK';
        var type = opts.type || 'info';

        var iconMap = {success: 'check-circle', error: 'x-circle', warning: 'alert-triangle', info: 'info'};
        var iconName = iconMap[type] || 'info';

        var overlay = document.createElement('div');
        overlay.className = 'modal-overlay active';
        overlay.setAttribute('role', 'dialog');
        overlay.setAttribute('aria-modal', 'true');
        overlay.innerHTML =
            '<div class="modal">' +
            '<div class="modal-header">' +
            '<h2><i data-lucide="' + iconName + '" class="icon-sm" style="margin-right:8px;"></i>' + escapeHtml(title) + '</h2>' +
            '</div>' +
            '<div class="modal-body">' +
            '<p>' + escapeHtml(message) + '</p>' +
            '</div>' +
            '<div class="modal-actions">' +
            '<button class="btn btn-primary alert-ok-btn">' + escapeHtml(buttonText) + '</button>' +
            '</div>' +
            '</div>';

        document.body.appendChild(overlay);
        if (typeof lucide !== 'undefined') {
            lucide.createIcons({nodes: [overlay]});
        }

        function close() {
            overlay.classList.remove('active');
            setTimeout(function () {
                overlay.remove();
            }, 200);
            resolve();
        }

        overlay.querySelector('.alert-ok-btn').addEventListener('click', close);
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) close();
        });
        overlay.querySelector('.alert-ok-btn').focus();
    });
};

// ─── Quantity Controls ────────────────────────────────
document.addEventListener('click', function (e) {
    var btn = e.target.closest('.qty-minus, .qty-plus');
    if (!btn) return;
    var input = btn.parentElement.querySelector('input[type="number"]');
    if (!input) return;
    var step = parseInt(input.getAttribute('step') || '1');
    var min = parseInt(input.getAttribute('min') || '1');
    var max = parseInt(input.getAttribute('max') || '999');
    var val = parseInt(input.value) || 1;
    if (btn.classList.contains('qty-minus')) {
        val = Math.max(min, val - step);
    } else {
        val = Math.min(max, val + step);
    }
    input.value = val;
    input.dispatchEvent(new Event('change', {bubbles: true}));
});

// ─── Scroll Reveal ────────────────────────────────────
function initScrollReveal() {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    var elements = document.querySelectorAll('.reveal, .reveal-left, .reveal-right, .reveal-scale');
    if (!elements.length) return;
    var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, {threshold: 0.15, rootMargin: '0px 0px -40px 0px'});
    elements.forEach(function (el) {
        observer.observe(el);
    });
}

// ─── Form Validation ──────────────────────────────────
function initFormValidation() {
    document.addEventListener('focus', function (e) {
        var input = e.target;
        if (!input.classList.contains('form-input') && !input.classList.contains('form-select')) return;
        input.dataset.touched = 'true';
    }, true);

    document.addEventListener('blur', function (e) {
        var input = e.target;
        if (!input.classList.contains('form-input') && !input.classList.contains('form-select')) return;
        if (!input.hasAttribute('required') && !input.hasAttribute('pattern') && input.type !== 'email') return;
        if (!input.dataset.touched) return;
        validateField(input);
    }, true);

    document.addEventListener('input', function (e) {
        var input = e.target;
        if (!input.classList.contains('form-input')) return;
        var group = input.closest('.form-group');
        if (group && group.classList.contains('error')) {
            validateField(input);
        }
        if (input.id === 'password' || input.name === 'password') {
            updatePasswordStrength(input.value);
        }
    }, true);
}

window.validateField = function (input) {
    var group = input.closest('.form-group');
    if (!group) return true;
    var errorEl = group.querySelector('.form-error');
    var valid = true;
    var message = '';

    if (input.hasAttribute('required') && !input.value.trim()) {
        valid = false;
        message = 'This field is required';
    } else if (input.type === 'email' && input.value) {
        var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(input.value)) {
            valid = false;
            message = 'Please enter a valid email address';
        }
    } else if (input.getAttribute('pattern') && input.value) {
        var regex = new RegExp(input.getAttribute('pattern'));
        if (!regex.test(input.value)) {
            valid = false;
            message = 'Please match the required format';
        }
    } else if (input.minLength > 0 && input.value && input.value.length < input.minLength) {
        valid = false;
        message = 'Minimum ' + input.minLength + ' characters required';
    }

    if (input.id === 'confirmPassword' || input.name === 'confirmPassword') {
        var passwordInput = document.getElementById('password') || document.querySelector('[name="password"]');
        if (passwordInput && input.value !== passwordInput.value) {
            valid = false;
            message = 'Passwords do not match';
        }
    }

    group.classList.remove('error', 'success');
    if (valid) {
        group.classList.add('success');
        if (errorEl) errorEl.textContent = '';
    } else {
        group.classList.add('error');
        if (errorEl) {
            errorEl.textContent = message;
        } else {
            errorEl = document.createElement('div');
            errorEl.className = 'form-error';
            errorEl.textContent = message;
            group.appendChild(errorEl);
        }
    }
    return valid;
};

function updatePasswordStrength(password) {
    var meter = document.querySelector('.password-strength');
    var textEl = document.querySelector('.password-strength-text');
    if (!meter) return;
    var score = 0;
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    meter.setAttribute('data-strength', score);
    if (textEl) {
        var labels = ['', 'Weak', 'Fair', 'Good', 'Strong'];
        var colors = ['', 'var(--color-danger)', 'var(--color-warning)', 'var(--color-primary)', 'var(--color-primary)'];
        textEl.textContent = password.length > 0 ? labels[score] || '' : '';
        textEl.style.color = colors[score] || '';
    }
}

// ─── Back to Top ──────────────────────────────────────
function initBackToTop() {
    var btn = document.querySelector('.back-to-top');
    if (!btn) return;
    window.addEventListener('scroll', window.debounce(function () {
        if (window.scrollY > 300) {
            btn.classList.add('visible');
        } else {
            btn.classList.remove('visible');
        }
    }, 100));
    btn.addEventListener('click', function () {
        window.scrollTo({top: 0, behavior: 'smooth'});
    });
}

// ─── Hero Parallax ────────────────────────────────────
function initHeroParallax() {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    if (window.innerWidth < 768) return;
    var heroImg = document.querySelector('.hero-image img');
    if (!heroImg) return;
    window.addEventListener('scroll', window.debounce(function () {
        var scrolled = window.scrollY;
        if (scrolled < 600) {
            heroImg.style.transform = 'translateY(' + (scrolled * 0.15) + 'px)';
        }
    }, 16));
}

// ─── Logout ──────────────────────────────────────────
window.handleLogout = function (e) {
    if (e) e.preventDefault();
    API.post('/auth/logout')
        .then(function () {
            window.location.href = '/login';
        })
        .catch(function () {
            window.location.href = '/login';
        });
};

// ─── Page Transitions ─────────────────────────────────
function initPageTransitions() {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    document.addEventListener('click', function (e) {
        var link = e.target.closest('a[href]');
        if (!link) return;
        var href = link.getAttribute('href');
        if (!href || href.startsWith('#') || href.startsWith('javascript:') || link.target === '_blank') return;
        if (link.origin !== window.location.origin) return;
        e.preventDefault();
        document.body.style.opacity = '0';
        document.body.style.transition = 'opacity 0.15s ease';
        setTimeout(function () {
            window.location.href = href;
        }, 150);
    });
    document.body.style.opacity = '1';
    document.body.style.transition = 'opacity 0.3s ease';
}
