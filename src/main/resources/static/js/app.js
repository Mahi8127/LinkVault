/**
 * LinkVault Frontend Application Controller
 * Handles URL shortening, clipboard copying, history listing, metrics, and link deletion.
 */

document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const shortenForm = document.getElementById('shortenForm');
    const urlInput = document.getElementById('urlInput');
    const shortenBtn = document.getElementById('shortenBtn');
    const resultCard = document.getElementById('resultCard');
    const resultShortUrl = document.getElementById('resultShortUrl');
    const copyResultBtn = document.getElementById('copyResultBtn');
    const visitResultBtn = document.getElementById('visitResultBtn');
    const closeResultBtn = document.getElementById('closeResultBtn');

    const totalLinksEl = document.getElementById('totalLinks');
    const totalClicksEl = document.getElementById('totalClicks');
    const historyCountBadge = document.getElementById('historyCountBadge');
    const refreshBtn = document.getElementById('refreshBtn');

    const urlTableBody = document.getElementById('urlTableBody');
    const emptyState = document.getElementById('emptyState');
    const loadingState = document.getElementById('loadingState');
    const toastContainer = document.getElementById('toastContainer');

    // Initial load
    fetchUrls();

    // Event Listeners
    shortenForm.addEventListener('submit', handleShortenSubmit);
    copyResultBtn.addEventListener('click', () => copyToClipboard(resultShortUrl.textContent, copyResultBtn));
    closeResultBtn.addEventListener('click', () => resultCard.classList.add('hidden'));
    refreshBtn.addEventListener('click', () => {
        refreshBtn.classList.add('spinning');
        fetchUrls().finally(() => refreshBtn.classList.remove('spinning'));
    });

    /**
     * Handles URL shortening form submission
     */
    async function handleShortenSubmit(e) {
        e.preventDefault();
        const inputUrl = urlInput.value.trim();

        if (!inputUrl) {
            showToast('Please enter a destination URL', 'error');
            return;
        }

        setButtonLoading(true);

        try {
            const response = await fetch('/api/urls/shorten', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ url: inputUrl })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Failed to shorten URL');
            }

            // Display result
            resultShortUrl.textContent = data.shortUrl;
            visitResultBtn.href = data.shortUrl;
            resultCard.classList.remove('hidden');

            // Reset form
            urlInput.value = '';
            showToast('Short URL generated successfully!', 'success');

            // Refresh table & stats
            fetchUrls();
        } catch (err) {
            showToast(err.message || 'An unexpected error occurred', 'error');
        } finally {
            setButtonLoading(false);
        }
    }

    /**
     * Fetches all shortened URLs from the backend
     */
    async function fetchUrls() {
        showLoading(true);
        try {
            const response = await fetch('/api/urls');
            if (!response.ok) {
                throw new Error(`Failed to load links (HTTP ${response.status})`);
            }

            const urls = await response.json();
            renderTable(urls);
            updateStats(urls);
        } catch (err) {
            console.error('Fetch error:', err);
            showToast('Unable to load URL history', 'error');
            renderTable([]);
        } finally {
            showLoading(false);
        }
    }

    /**
     * Renders the URLs table rows
     */
    function renderTable(urls) {
        urlTableBody.innerHTML = '';

        if (!urls || urls.length === 0) {
            emptyState.classList.remove('hidden');
            return;
        }

        emptyState.classList.add('hidden');

        urls.forEach(item => {
            const row = document.createElement('tr');
            row.id = `row-${item.id}`;

            const formattedDate = formatDate(item.createdAt);

            row.innerHTML = `
                <td>
                    <div class="cell-original-url" title="${escapeHtml(item.originalUrl)}">
                        ${escapeHtml(item.originalUrl)}
                    </div>
                </td>
                <td>
                    <div class="cell-short-link">
                        <a href="${escapeHtml(item.shortUrl)}" target="_blank" rel="noopener noreferrer" class="short-link-anchor">
                            ${escapeHtml(item.shortUrl)}
                        </a>
                        <button class="copy-mini-btn" title="Copy link" onclick="window.linkVaultCopy('${escapeHtml(item.shortUrl)}', this)">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                            </svg>
                        </button>
                    </div>
                </td>
                <td>
                    <span class="click-badge">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                        ${item.clickCount}
                    </span>
                </td>
                <td>
                    <span class="date-text">${formattedDate}</span>
                </td>
                <td style="text-align: center;">
                    <button class="btn-danger-icon" title="Delete short link" onclick="window.linkVaultDelete('${item.id}')">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="3 6 5 6 21 6"></polyline>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            <line x1="10" y1="11" x2="10" y2="17"></line>
                            <line x1="14" y1="11" x2="14" y2="17"></line>
                        </svg>
                    </button>
                </td>
            `;

            urlTableBody.appendChild(row);
        });
    }

    /**
     * Deletes a short URL by its MongoDB ID
     */
    async function deleteUrl(id) {
        if (!confirm('Are you sure you want to delete this short link?')) {
            return;
        }

        try {
            const response = await fetch(`/api/urls/${id}`, {
                method: 'DELETE'
            });

            if (response.status === 204) {
                showToast('Link deleted successfully', 'success');
                const row = document.getElementById(`row-${id}`);
                if (row) row.remove();
                fetchUrls();
            } else if (response.status === 404) {
                showToast('Link not found or already deleted', 'error');
                fetchUrls();
            } else {
                throw new Error('Failed to delete URL');
            }
        } catch (err) {
            console.error('Delete error:', err);
            showToast('Unable to delete link', 'error');
        }
    }

    /**
     * Updates top-level metric counters
     */
    function updateStats(urls) {
        const total = urls ? urls.length : 0;
        const clicks = urls ? urls.reduce((acc, item) => acc + (item.clickCount || 0), 0) : 0;

        totalLinksEl.textContent = total;
        totalClicksEl.textContent = clicks;
        historyCountBadge.textContent = `${total} link${total === 1 ? '' : 's'}`;
    }

    /**
     * Copies text to clipboard with fallback and visual feedback
     */
    async function copyToClipboard(text, btnElement) {
        let copied = false;
        if (navigator.clipboard && window.isSecureContext) {
            try {
                await navigator.clipboard.writeText(text);
                copied = true;
            } catch (e) {
                copied = fallbackCopy(text);
            }
        } else {
            copied = fallbackCopy(text);
        }

        if (copied) {
            const originalHTML = btnElement.innerHTML;
            if (btnElement.classList.contains('copy-mini-btn')) {
                btnElement.innerHTML = `
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                `;
            } else {
                btnElement.innerHTML = `
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    <span style="color:#10b981;">Copied!</span>
                `;
            }
            showToast('Link copied to clipboard!', 'success');
            setTimeout(() => {
                btnElement.innerHTML = originalHTML;
            }, 2000);
        } else {
            showToast('Unable to copy automatically. Please copy manually.', 'error');
        }
    }

    function fallbackCopy(text) {
        try {
            const textarea = document.createElement('textarea');
            textarea.value = text;
            textarea.style.position = 'fixed';
            textarea.style.left = '-9999px';
            document.body.appendChild(textarea);
            textarea.focus();
            textarea.select();
            const successful = document.execCommand('copy');
            document.body.removeChild(textarea);
            return successful;
        } catch {
            return false;
        }
    }

    /**
     * Formats ISO date string to a human-readable local date
     */
    function formatDate(isoString) {
        if (!isoString) return 'Just now';
        try {
            const date = new Date(isoString);
            return date.toLocaleDateString(undefined, {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return isoString;
        }
    }

    /**
     * Toggles loading spinner
     */
    function showLoading(isLoading) {
        if (isLoading && urlTableBody.children.length === 0) {
            loadingState.classList.remove('hidden');
        } else {
            loadingState.classList.add('hidden');
        }
    }

    /**
     * Sets button state during submission
     */
    function setButtonLoading(isLoading) {
        shortenBtn.disabled = isLoading;
        const btnText = shortenBtn.querySelector('.btn-text');
        if (isLoading) {
            btnText.textContent = 'Shortening...';
        } else {
            btnText.textContent = 'Shorten Link';
        }
    }

    /**
     * Displays a temporary toast notification
     */
    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;

        toastContainer.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // Expose methods needed by inline onclick handlers
    window.linkVaultDelete = deleteUrl;
    window.linkVaultCopy = copyToClipboard;
});
