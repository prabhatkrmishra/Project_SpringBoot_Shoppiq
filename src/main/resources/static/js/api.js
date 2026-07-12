/* ─── API Utility ─────────────────────────────────────────
 *  Centralised fetch wrapper for all frontend → backend
 *  communication. Uses cookies (credentials: "include")
 *  for JWT auth — no manual token handling required.
 *  Auto-refreshes access token on 401 for mutating requests.
 * ──────────────────────────────────────────────────────── */

window.API = (function () {

    var isRefreshing = false;
    var refreshPromise = null;

    async function refreshAccessToken() {
        if (refreshPromise) return refreshPromise;
        isRefreshing = true;
        refreshPromise = fetch("/auth/refresh", {
            method: "POST",
            credentials: "include",
            headers: {"Accept": "application/json"}
        })
            .then(function (response) {
                if (!response.ok) throw new Error("Refresh failed");
                return response.json().catch(function () {
                    return null;
                });
            })
            .catch(function (err) {
                window.location.href = "/login?expired=1";
                throw err;
            })
            .finally(function () {
                isRefreshing = false;
                refreshPromise = null;
            });
        return refreshPromise;
    }

    async function request(url, options) {
        var defaults = {
            credentials: "include",
            headers: {"Accept": "application/json"}
        };
        var config = Object.assign({}, defaults, options || {});
        if (config.body && typeof config.body === "object" && !(config.body instanceof FormData)) {
            config.headers["Content-Type"] = "application/json";
            config.body = JSON.stringify(config.body);
        }
        var isMutating = config.method && ["POST", "PUT", "PATCH", "DELETE"].includes(config.method.toUpperCase());
        try {
            var response = await fetch(url, config);
            if (response.status === 401 && isMutating && !config._retry) {
                await refreshAccessToken();
                config._retry = true;
                response = await fetch(url, config);
            }
            if (response.status === 204) return null;
            var data = await response.json().catch(function () {
                return null;
            });
            if (!response.ok) {
                var message = (data && data.message) || (data && data.detail) || (data && data.error) || "Request failed (" + response.status + ")";
                throw {status: response.status, message: message, data: data};
            }
            return data;
        } catch (err) {
            if (err.status) throw err;
            throw {status: 0, message: "Unable to connect to server", data: null};
        }
    }

    return {
        get: function (url) {
            return request(url, {method: "GET"});
        },
        post: function (url, body) {
            return request(url, {method: "POST", body: body});
        },
        put: function (url, body) {
            return request(url, {method: "PUT", body: body});
        },
        patch: function (url, body) {
            return request(url, {method: "PATCH", body: body});
        },
        del: function (url) {
            return request(url, {method: "DELETE"});
        },
        getBlob: function (url) {
            return fetch(url, {credentials: "include", headers: {"Accept": "*/*"}})
                .then(function (r) {
                    if (!r.ok) throw {status: r.status, message: "Download failed", data: null};
                    return r.blob();
                });
        }
    };

})();

/* ─── Shared UI Helpers ────────────────────────────────── */

window.formatCurrency = function (n) {
    if (n == null || isNaN(n)) return '$0.00';
    return new Intl.NumberFormat('en-US', {style: 'currency', currency: 'USD'}).format(n);
};

window.formatDate = function (iso) {
    if (!iso) return '';
    try {
        return new Intl.DateTimeFormat('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        }).format(new Date(iso));
    } catch (e) {
        return iso;
    }
};

window.formatDateTime = function (iso) {
    if (!iso) return '';
    try {
        return new Intl.DateTimeFormat('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: 'numeric',
            minute: '2-digit'
        }).format(new Date(iso));
    } catch (e) {
        return iso;
    }
};

window.initIcons = function () {
    if (typeof lucide !== 'undefined' && lucide.createIcons) {
        lucide.createIcons();
    }
};

window.setButtonLoading = function (btn, loading) {
    if (!btn) return;
    if (loading) {
        btn.disabled = true;
        btn.dataset.originalText = btn.innerHTML;
        btn.innerHTML = '<span class="spinner"></span> Please wait...';
    } else {
        btn.disabled = false;
        if (btn.dataset.originalText) btn.innerHTML = btn.dataset.originalText;
        delete btn.dataset.originalText;
    }
};

window.escapeHtml = function (str) {
    if (!str) return '';
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
};
