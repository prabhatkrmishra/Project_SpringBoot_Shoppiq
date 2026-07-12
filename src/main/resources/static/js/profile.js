(() => {
    'use strict';

    document.addEventListener('DOMContentLoaded', () => {
        initTabs();
        initProfileModal();
        initSecurityTab();
        initNotificationsTab();
        initAddressesTab();
        initEmailVerification();
    });

    function initTabs() {
        const tabs = document.querySelectorAll('.tab');
        const tabContents = document.querySelectorAll('.tab-content');

        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const target = tab.dataset.tab;

                tabs.forEach(t => {
                    t.classList.remove('active');
                    t.setAttribute('aria-selected', 'false');
                });
                tabContents.forEach(c => c.classList.remove('active'));

                tab.classList.add('active');
                tab.setAttribute('aria-selected', 'true');
                const content = document.getElementById(`tab-${target}`);
                if (content) content.classList.add('active');
            });
        });
    }

    function initProfileModal() {
        const openBtn = document.getElementById('open-update-modal');
        const modal = document.getElementById('update-profile-modal');
        const closeBtn = document.getElementById('close-update-modal');
        const cancelBtn = document.getElementById('cancel-update-modal');
        const saveBtn = document.getElementById('save-update-modal');
        const nameInput = document.getElementById('modal-name');
        const emailInput = document.getElementById('modal-email');

        if (!openBtn || !modal) return;

        openBtn.addEventListener('click', () => {
            const currentName = document.getElementById('display-name').textContent.trim();
            const currentEmail = document.getElementById('display-email').textContent.trim();
            nameInput.value = currentName;
            emailInput.value = currentEmail;
            clearFieldError(nameInput);
            modal.classList.add('active');
            nameInput.focus();
        });

        const closeModal = () => {
            modal.classList.remove('active');
            clearFieldError(nameInput);
        };

        closeBtn.addEventListener('click', closeModal);
        cancelBtn.addEventListener('click', closeModal);

        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal();
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && modal.classList.contains('active')) {
                closeModal();
            }
        });

        saveBtn.addEventListener('click', async () => {
            const name = nameInput.value.trim();
            if (!name) return showError(nameInput, 'Name is required');

            try {
                await API.put('/user/profile', {name});
                document.getElementById('display-name').textContent = name;
                showSuccess('Profile updated successfully');
                closeModal();
            } catch (err) {
                showError(nameInput, err.message || 'Failed to update profile');
            }
        });
    }

    function initSecurityTab() {
        initPasswordForm();
    }

    function initNotificationsTab() {
        loadNotificationPreferences();

        const saveBtn = document.getElementById('save-notifications-btn');
        if (!saveBtn) return;

        saveBtn.addEventListener('click', async () => {
            const prefs = {
                orderUpdates: document.querySelector('input[name="orderUpdates"]').checked,
                accountSecurity: document.querySelector('input[name="accountSecurity"]').checked,
                promotions: document.querySelector('input[name="promotions"]').checked,
                reviewsEngagement: document.querySelector('input[name="reviews"]').checked
            };

            try {
                await API.put('/user/notifications', prefs);
                showSuccess('Notification preferences saved');
            } catch (err) {
                showError(saveBtn, err.message || 'Failed to save preferences');
            }
        });
    }

    function initAddressesTab() {
        const select = document.getElementById('default-address-select');
        const btn = document.getElementById('save-address-btn');
        if (!select || !btn) return;

        loadAddresses(select);

        btn.addEventListener('click', async () => {
            const addressId = select.value;
            if (!addressId) return showError(select, 'Please select an address');

            try {
                await API.put(`/user/address/default/${addressId}`, {});
                showSuccess('Default address updated');
            } catch (err) {
                showError(select, err.message || 'Failed to update default address');
            }
        });
    }

    async function loadAddresses(select) {
        try {
            const addresses = await API.get('/user/address/get/all');
            select.innerHTML = '<option value="">Select default address</option>';
            addresses.forEach(addr => {
                const opt = document.createElement('option');
                opt.value = addr.id;
                opt.textContent = addr.label || `${addr.city}, ${addr.country}`;
                if (addr.default) opt.selected = true;
                select.appendChild(opt);
            });
            select.disabled = false;
        } catch (err) {
            select.innerHTML = '<option value="">Failed to load addresses</option>';
        }
    }

    async function loadNotificationPreferences() {
        try {
            const prefs = await API.get('/user/notifications');
            if (prefs) {
                const orderUpdates = document.querySelector('input[name="orderUpdates"]');
                const accountSecurity = document.querySelector('input[name="accountSecurity"]');
                const promotions = document.querySelector('input[name="promotions"]');
                const reviews = document.querySelector('input[name="reviews"]');
                if (orderUpdates) orderUpdates.checked = prefs.orderUpdates;
                if (accountSecurity) accountSecurity.checked = prefs.accountSecurity;
                if (promotions) promotions.checked = prefs.promotions;
                if (reviews) reviews.checked = prefs.reviewsEngagement;
            }
        } catch (err) {
            // defaults stay as-is if load fails
        }
    }

    function initPasswordForm() {
        const form = document.getElementById('password-form');
        const newPassword = document.getElementById('newPassword');
        const currentGroup = document.getElementById('current-password-group');
        const strengthFill = document.getElementById('strength-fill');
        const strengthText = document.getElementById('strength-text');

        if (!form) return;

        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            clearErrors(form);

            const data = {
                currentPassword: document.getElementById('currentPassword')?.value || null,
                newPassword: newPassword.value,
                confirmPassword: document.getElementById('confirmPassword').value
            };

            const reqs = validatePasswordRequirements(data.newPassword);
            if (!reqs.length) return showError(newPassword, 'Password must be at least 8 characters');
            if (!reqs.upper) return showError(newPassword, 'Password must contain an uppercase letter');
            if (!reqs.lower) return showError(newPassword, 'Password must contain a lowercase letter');
            if (!reqs.digit) return showError(newPassword, 'Password must contain a number');
            if (!reqs.special) return showError(newPassword, 'Password must contain a special character (@$!%*?&)');
            if (data.newPassword !== data.confirmPassword) return showError(document.getElementById('confirmPassword'), 'Passwords do not match');

            try {
                await API.put('/user/password', data);
                showSuccess('Password updated. Please sign in again.');
                setTimeout(() => window.location.href = '/login', 1500);
            } catch (err) {
                if (err.status === 400 && err.message.toLowerCase().includes('current password')) {
                    showError(document.getElementById('currentPassword'), 'Current password is incorrect');
                } else {
                    showError(form, err.message || 'Failed to update password');
                }
            }
        });

        if (newPassword) {
            var reqsEl = newPassword.closest('.form-group').querySelector('.password-requirements');
            var confirmPw = document.getElementById('confirmPassword');

            function toggleReqs() {
                if (!reqsEl) return;
                var pw = newPassword.value;
                var cpw = confirmPw ? confirmPw.value : '';
                if (pw && cpw && pw === cpw) {
                    reqsEl.classList.remove('visible');
                } else if (pw) {
                    reqsEl.classList.add('visible');
                } else {
                    reqsEl.classList.remove('visible');
                }
            }

            newPassword.addEventListener('focus', () => {
                if (newPassword.value) reqsEl.classList.add('visible');
            });
            newPassword.addEventListener('input', () => {
                const strength = calculateStrength(newPassword.value);
                strengthFill.className = 'strength-fill';
                if (strength.level) strengthFill.classList.add(strength.level);
                strengthText.textContent = strength.label;
                updatePasswordRequirements(newPassword.value);
                toggleReqs();
            });
            if (confirmPw) {
                confirmPw.addEventListener('input', toggleReqs);
            }
        }

        checkHasPassword().then(hasPassword => {
            if (currentGroup) currentGroup.style.display = hasPassword ? 'block' : 'none';
        });
    }

    async function checkHasPassword() {
        try {
            const res = await API.get('/user/profile');
            return res.hasPassword === true;
        } catch {
            return true;
        }
    }

    function calculateStrength(password) {
        if (!password) return {level: '', label: 'Password strength'};
        let score = 0;
        if (password.length >= 8) score += 20;
        if (/[A-Z]/.test(password)) score += 20;
        if (/[a-z]/.test(password)) score += 20;
        if (/[0-9]/.test(password)) score += 20;
        if (/[@$!%*?&]/.test(password)) score += 20;

        if (score < 40) return {level: 'weak', label: 'Weak'};
        if (score < 60) return {level: 'fair', label: 'Fair'};
        if (score < 80) return {level: 'good', label: 'Good'};
        return {level: 'strong', label: 'Strong'};
    }

    function validatePasswordRequirements(password) {
        return {
            length: password.length >= 8,
            upper: /[A-Z]/.test(password),
            lower: /[a-z]/.test(password),
            digit: /[0-9]/.test(password),
            special: /[@$!%*?&]/.test(password)
        };
    }

    function updatePasswordRequirements(password) {
        const reqs = validatePasswordRequirements(password);
        const map = {
            'req-length': reqs.length,
            'req-upper': reqs.upper,
            'req-lower': reqs.lower,
            'req-digit': reqs.digit,
            'req-special': reqs.special
        };
        Object.entries(map).forEach(([id, met]) => {
            const el = document.getElementById(id);
            if (el) el.classList.toggle('met', met);
        });
    }

    function validatePasswordRequirements(password) {
        return {
            length: password.length >= 8,
            upper: /[A-Z]/.test(password),
            lower: /[a-z]/.test(password),
            digit: /[0-9]/.test(password),
            special: /[@$!%*?&]/.test(password)
        };
    }

    function updatePasswordRequirements(password) {
        const reqs = validatePasswordRequirements(password);
        document.getElementById("req-length")?.classList.toggle("met", reqs.length);
        document.getElementById("req-upper")?.classList.toggle("met", reqs.upper);
        document.getElementById("req-lower")?.classList.toggle("met", reqs.lower);
        document.getElementById("req-digit")?.classList.toggle("met", reqs.digit);
        document.getElementById("req-special")?.classList.toggle("met", reqs.special);
    }

    function showError(el, msg) {
        const errorEl = document.getElementById(`${el.id}-error`) || findErrorEl(el);
        if (errorEl) errorEl.textContent = msg;
        el.classList.add('is-invalid');
    }

    function findErrorEl(input) {
        const group = input.closest('.form-group') || input.closest('.inline-edit-wrap') || input.closest('.address-select-row');
        return group?.querySelector('.form-error');
    }

    function clearFieldError(el) {
        el.classList.remove('is-invalid');
        const errorEl = document.getElementById(`${el.id}-error`) || findErrorEl(el);
        if (errorEl) errorEl.textContent = '';
    }

    function clearErrors(form) {
        form.querySelectorAll('.form-error').forEach(e => e.textContent = '');
        form.querySelectorAll('.is-invalid').forEach(e => e.classList.remove('is-invalid'));
    }

    function showSuccess(msg) {
        const toast = document.createElement('div');
        toast.className = 'toast toast-success';
        toast.textContent = msg;
        document.getElementById('toast-container')?.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
    }

    function initEmailVerification() {
        const badge = document.getElementById('verification-badge');
        const verifyBtn = document.getElementById('verify-email-btn');
        const modal = document.getElementById('verify-email-modal');
        const closeBtn = document.getElementById('close-verify-modal');
        const cancelBtn = document.getElementById('cancel-verify-modal');
        const submitBtn = document.getElementById('submit-verify-btn');
        const codeInput = document.getElementById('verifyCode');

        if (!badge) return;

        loadVerificationStatus();

        async function loadVerificationStatus() {
            try {
                const profile = await API.get('/user/profile');
                if (profile.emailVerified) {
                    badge.textContent = 'Verified';
                    badge.className = 'status-badge active';
                    verifyBtn?.classList.add('hidden');
                } else {
                    badge.textContent = 'Not Verified';
                    badge.className = 'status-badge pending';
                    verifyBtn?.classList.remove('hidden');
                }
            } catch {
                badge.textContent = 'Unknown';
                badge.className = 'status-badge';
            }
        }

        verifyBtn?.addEventListener('click', async () => {
            try {
                const profile = await API.get('/user/profile');
                await API.post('/auth/verify-email', {email: profile.email});
                showToast('Verification code sent to your email', 'success');
                modal.classList.add('active');
                codeInput.focus();
            } catch {
                showToast('Failed to send verification code', 'error');
            }
        });

        const closeModal = () => {
            modal.classList.remove('active');
            codeInput.value = '';
            document.getElementById('verifyCode-error').textContent = '';
        };

        closeBtn?.addEventListener('click', closeModal);
        cancelBtn?.addEventListener('click', closeModal);
        modal?.addEventListener('click', (e) => {
            if (e.target === modal) closeModal();
        });

        submitBtn?.addEventListener('click', async () => {
            const code = codeInput.value.trim();
            if (!code || code.length !== 6 || !/^\d+$/.test(code)) {
                document.getElementById('verifyCode-error').textContent = 'Please enter a valid 6-digit code';
                return;
            }
            try {
                const profile = await API.get('/user/profile');
                await API.post('/auth/confirm-email', {email: profile.email, code});
                showToast('Email verified successfully', 'success');
                closeModal();
                loadVerificationStatus();
            } catch (err) {
                document.getElementById('verifyCode-error').textContent = err.message || 'Invalid verification code';
            }
        });
    }
})();
