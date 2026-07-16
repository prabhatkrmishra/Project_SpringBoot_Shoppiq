window.AIChat = (function () {

    var chatId = null;
    var isOpen = false;
    var isStreaming = false;
    var isResolved = false;
    var isLoggedIn = false;
    var selectedModel = localStorage.getItem('ai-chat-model') || 'nvidia/llama-3.3-nemotron-super-49b-v1.5';

    return {
        init: init, toggle: toggle
    };

    function init() {
        isLoggedIn = !!document.querySelector('meta[name="shoppiq-authenticated"]');

        var userIdMeta = document.querySelector('meta[name="shoppiq-user-id"]');
        var userId = userIdMeta ? userIdMeta.getAttribute('content') : null;
        if (userId && userId !== 'null') {
            document.getElementById('ai-chat-user-id').textContent = '#' + userId;
        }

        var savedChatId = localStorage.getItem('ai-chat-id');
        if (savedChatId) {
            chatId = savedChatId;
        }

        document.getElementById('ai-chat-toggle')
            .addEventListener('click', toggle);
        document.getElementById('ai-chat-close')
            .addEventListener('click', toggle);
        document.getElementById('ai-chat-send')
            .addEventListener('click', sendMessage);
        document.getElementById('ai-chat-resolve-btn')
            .addEventListener('click', resolveChat);
        document.getElementById('ai-chat-history-btn')
            .addEventListener('click', toggleSidebar);
        document.getElementById('ai-chat-new')
            .addEventListener('click', newConversation);
        document.getElementById('ai-chat-new-after-resolve')
            .addEventListener('click', newConversation);

        var input = document.getElementById('ai-chat-input');
        input.addEventListener('input', function () {
            this.style.height = 'auto';
            this.style.height = Math.min(this.scrollHeight, 120) + 'px';
        });

        input.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        var modelSelect = document.getElementById('ai-model-select');
        modelSelect.value = selectedModel;
        modelSelect.addEventListener('change', function () {
            selectedModel = this.value;
            localStorage.setItem('ai-chat-model', selectedModel);
        });

        initIcons();

        if (chatId) {
            loadHistory();
        }
    }

    function toggle() {
        isOpen = !isOpen;
        var widget = document.getElementById('ai-chat-widget');
        widget.setAttribute('aria-hidden', !isOpen);
        if (isOpen) {
            document.getElementById('ai-chat-input').focus();
            scrollToBottom();
        }
    }

    function sendMessage() {
        var input = document.getElementById('ai-chat-input');
        var text = input.value.trim();
        if (!text || isStreaming) return;

        appendMessage('user', text);
        input.value = '';
        input.style.height = 'auto';
        isStreaming = true;
        updateSendButton();
        showTypingIndicator();

        var endpoint;
        var body = {message: text, model: selectedModel};

        if (isLoggedIn) {
            if (chatId) {
                endpoint = '/api/ai/chat/' + chatId;
            } else {
                endpoint = '/api/ai/chat';
            }
        } else {
            endpoint = '/api/ai/guest';
        }

        fetch(endpoint, {
            method: 'POST',
            credentials: 'include',
            headers: Object.assign({'Content-Type': 'application/json'}, csrfHeaders()),
            body: JSON.stringify(body)
        }).then(function (response) {
            if (!response.ok) {
                return response.json().then(function (data) {
                    var msg = (data && (data.detail || data.error || data.message)) || 'AI assistant is temporarily unavailable' || 'Request failed';
                    throw new Error(msg);
                }).catch(function (parseErr) {
                    if (parseErr.message && parseErr.message !== 'AI assistant is temporarily unavailable') throw parseErr;
                    throw new Error('AI assistant is temporarily unavailable. Please try again.');
                });
            }
            return response.json();
        }).then(function (data) {
            removeTypingIndicator();

            if (isLoggedIn && data.messages && data.messages.length > 0) {
                var lastAssistant = null;
                for (var i = data.messages.length - 1; i >= 0; i--) {
                    if (data.messages[i].role === 'ASSISTANT') {
                        lastAssistant = data.messages[i];
                        break;
                    }
                }
                if (lastAssistant) {
                    appendMessage('assistant', lastAssistant.content);
                }
                if (data.chatId) {
                    chatId = data.chatId;
                    localStorage.setItem('ai-chat-id', chatId);
                }
            } else if (data.response) {
                appendMessage('assistant', data.response);
                if (data.sessionId) {
                    chatId = data.sessionId;
                    localStorage.setItem('ai-chat-id', chatId);
                }
            }

            updateChatIdDisplay();
        }).catch(function (err) {
            removeTypingIndicator();
            showToast(err.message || 'Unable to connect to AI assistant', 'error');
        }).finally(function () {
            isStreaming = false;
            updateSendButton();
        });
    }

    function resolveChat() {
        if (!chatId || isResolved) return;

        var endpoint = isLoggedIn ? '/api/ai/chat/' + chatId : '/api/ai/guest/' + chatId;

        fetch(endpoint, {
            method: 'DELETE', credentials: 'include', headers: csrfHeaders()
        }).then(function (response) {
            if (!response.ok) throw {status: response.status};
            markResolved();
            showToast('Conversation marked as resolved', 'success');
        }).catch(function (err) {
            showToast(err.message || 'Failed to resolve conversation', 'error');
        });
    }

    function markResolved() {
        isResolved = true;
        document.getElementById('ai-chat-resolved-banner').setAttribute('aria-hidden', 'false');
        document.getElementById('ai-chat-input-area').setAttribute('aria-hidden', 'true');
        appendSystemMessage('This conversation has been resolved.');
    }

    function toggleSidebar() {
        var sidebar = document.getElementById('ai-chat-sidebar');
        var isHidden = sidebar.getAttribute('aria-hidden') === 'true';
        sidebar.setAttribute('aria-hidden', !isHidden);
        if (isHidden) loadConversationList();
    }

    function loadConversationList() {
        if (!isLoggedIn) return;
        API.get('/api/ai/chat/conversations').then(function (conversations) {
            var list = document.getElementById('ai-chat-sidebar-list');
            list.innerHTML = '';

            if (!conversations || conversations.length === 0) {
                list.innerHTML = '<div class="ai-chat-sidebar-empty">No conversations yet.</div>';
                return;
            }

            conversations.forEach(function (conv) {
                var el = document.createElement('div');
                el.className = 'ai-chat-sidebar-item';
                if (conv.chatId === chatId) el.classList.add('active');
                el.innerHTML = '<div class="ai-chat-sidebar-item-title">' + escapeHtml(conv.title) + '<span class="ai-chat-status-badge ' + conv.status.toLowerCase() + '">' + conv.status + '</span>' + '</div>' + '<div class="ai-chat-sidebar-item-meta">' + escapeHtml(conv.chatId) + ' &middot; ' + conv.messageCount + ' messages' + '</div>';
                el.addEventListener('click', function () {
                    loadConversation(conv.chatId);
                });
                list.appendChild(el);
            });
        }).catch(function () {
            var list = document.getElementById('ai-chat-sidebar-list');
            list.innerHTML = '<div class="ai-chat-sidebar-empty">Failed to load conversations.</div>';
        });
    }

    function loadConversation(targetChatId) {
        API.get('/api/ai/chat/' + targetChatId + '/messages').then(function (data) {
            chatId = targetChatId;
            isResolved = false;

            localStorage.setItem('ai-chat-id', chatId);

            var container = document.getElementById('ai-chat-messages');
            container.innerHTML = '';

            renderServerMessages(data || []);
            updateChatIdDisplay();

            document.getElementById('ai-chat-resolved-banner').setAttribute('aria-hidden', 'true');
            document.getElementById('ai-chat-input-area').setAttribute('aria-hidden', 'false');

            toggleSidebar();
        });
    }

    function loadHistory() {
        if (!chatId) return;

        var endpoint = isLoggedIn ? '/api/ai/chat/' + chatId + '/messages' : '/api/ai/guest/' + chatId + '/messages';

        fetch(endpoint, {
            method: 'GET', credentials: 'include', headers: {'Accept': 'application/json'}
        }).then(function (response) {
            if (!response.ok) {
                chatId = null;
                localStorage.removeItem('ai-chat-id');
                return null;
            }
            return response.json();
        }).then(function (data) {
            if (!data || !data.length) return;

            var container = document.getElementById('ai-chat-messages');
            container.innerHTML = '';

            renderServerMessages(data);
            updateChatIdDisplay();

            var lastMsg = data[data.length - 1];
            if (lastMsg && lastMsg.role === 'SYSTEM' && lastMsg.content && lastMsg.content.toLowerCase().includes('resolved')) {
                isResolved = true;
                document.getElementById('ai-chat-resolved-banner').setAttribute('aria-hidden', 'false');
                document.getElementById('ai-chat-input-area').setAttribute('aria-hidden', 'true');
            }
        }).catch(function () {
            chatId = null;
            localStorage.removeItem('ai-chat-id');
        });
    }

    function newConversation() {
        chatId = null;
        isResolved = false;
        localStorage.removeItem('ai-chat-id');

        var container = document.getElementById('ai-chat-messages');
        var welcomeHtml = isLoggedIn ? '<div class="ai-chat-welcome">' + '<p>Hi! I\'m Shoppiq\'s AI assistant.</p>' + '<p>I can help you find products, check orders, and more.</p>' + '</div>' : '<div class="ai-chat-welcome">' + '<p>Hi! I\'m Shoppiq\'s AI assistant.</p>' + '<p>I can help you discover products. Sign in to check orders and manage your cart.</p>' + '</div>';
        container.innerHTML = welcomeHtml;

        document.getElementById('ai-chat-id').textContent = '';
        document.getElementById('ai-chat-resolved-banner').setAttribute('aria-hidden', 'true');
        document.getElementById('ai-chat-input-area').setAttribute('aria-hidden', 'false');
        document.getElementById('ai-chat-input').value = '';
        document.getElementById('ai-chat-input').style.height = 'auto';
    }

    function renderServerMessages(messages) {
        messages.forEach(function (msg) {
            if (msg.role === 'USER') {
                appendMessage('user', msg.content);
            } else if (msg.role === 'ASSISTANT') {
                appendMessage('assistant', msg.content);
            } else if (msg.role === 'SYSTEM') {
                if (msg.content && msg.content.toLowerCase().includes('resolved')) {
                    appendSystemMessage('This conversation has been resolved.');
                }
            }
        });
    }

    function appendMessage(role, content) {
        var container = document.getElementById('ai-chat-messages');
        var welcome = container.querySelector('.ai-chat-welcome');
        if (welcome) welcome.remove();

        var el = document.createElement('div');
        el.className = 'ai-chat-msg ' + role;
        el.innerHTML = formatMessageContent(content);
        container.appendChild(el);
        scrollToBottom();
        return el;
    }

    function appendSystemMessage(content) {
        var container = document.getElementById('ai-chat-messages');
        var el = document.createElement('div');
        el.className = 'ai-chat-msg system';
        el.textContent = content;
        container.appendChild(el);
        scrollToBottom();
    }

    function showTypingIndicator() {
        removeTypingIndicator();
        var container = document.getElementById('ai-chat-messages');
        var el = document.createElement('div');
        el.className = 'ai-typing-indicator';
        el.id = 'ai-typing-indicator';
        el.innerHTML = '<span></span><span></span><span></span>';
        container.appendChild(el);
        scrollToBottom();
    }

    function removeTypingIndicator() {
        var el = document.getElementById('ai-typing-indicator');
        if (el) el.remove();
    }

    function scrollToBottom() {
        var container = document.getElementById('ai-chat-messages');
        container.scrollTop = container.scrollHeight;
    }

    function updateSendButton() {
        var btn = document.getElementById('ai-chat-send');
        btn.disabled = isStreaming;
    }

    function updateChatIdDisplay() {
        document.getElementById('ai-chat-id').textContent = chatId || '';
    }

    function formatMessageContent(content) {
        if (!content) return '';
        var safe = escapeHtml(content);
        safe = safe.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        safe = safe.replace(/(\/item\/[\w-]+)/g, '<a href="$1" target="_blank">$1</a>');
        safe = safe.replace(/\n/g, '<br>');
        return safe;
    }

    function csrfHeaders() {
        var match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
        var token = match ? decodeURIComponent(match[1]) : null;
        return token ? {'X-XSRF-TOKEN': token} : {};
    }

    function initIcons() {
        if (typeof lucide !== 'undefined' && lucide.createIcons) {
            lucide.createIcons();
        }
    }

})();

document.addEventListener('DOMContentLoaded', function () {
    window.AIChat.init();
});
