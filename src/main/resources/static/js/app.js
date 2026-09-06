/**
 * FarmConnect AI - Client-side Interactive Functions (SIH26033)
 */

document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alert banners after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 6000);
    });

    // Check unread notifications periodically if user is logged in
    const notifBadge = document.getElementById('navbar-notif-badge');
    if (notifBadge) {
        fetchUnreadCount();
        setInterval(fetchUnreadCount, 30000);
    }
});

function fetchUnreadCount() {
    fetch('/notifications/unread-count')
        .then(response => response.json())
        .then(data => {
            const notifBadge = document.getElementById('navbar-notif-badge');
            if (notifBadge && data.unreadCount !== undefined) {
                if (data.unreadCount > 0) {
                    notifBadge.textContent = data.unreadCount;
                    notifBadge.classList.remove('d-none');
                } else {
                    notifBadge.classList.add('d-none');
                }
            }
        })
        .catch(err => console.debug('Notifications polling:', err));
}

function markNotificationRead(id, buttonElem) {
    fetch('/notifications/read/' + id, {
        method: 'POST',
        headers: {
            'X-CSRF-TOKEN': getCsrfToken()
        }
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            const item = document.getElementById('notif-item-' + id);
            if (item) {
                item.classList.remove('bg-light');
                item.classList.add('opacity-75');
            }
            if (buttonElem) buttonElem.remove();
            fetchUnreadCount();
        }
    });
}

function getCsrfToken() {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    return tokenMeta ? tokenMeta.getAttribute('content') : '';
}

// Interactive helper for Farmer Add Crop Price Comparison
function checkMarketComparison(cropInputId, priceInputId, targetDisplayId) {
    const crop = document.getElementById(cropInputId)?.value;
    const price = document.getElementById(priceInputId)?.value;
    const display = document.getElementById(targetDisplayId);

    if (!crop || !price || !display) return;

    fetch('/market/compare?crop=' + encodeURIComponent(crop) + '&price=' + encodeURIComponent(price))
        .then(res => res.json())
        .then(data => {
            if (data.hasComparison) {
                display.innerHTML = `
                    <div class="alert alert-info py-2 px-3 mb-0 mt-2 small">
                        <strong><i class="bi bi-info-circle-fill me-1"></i> Market Comparison:</strong>
                        Prevailing price in ${data.marketName || 'nearby mandis'} is <strong>?${data.marketPrice}/kg</strong>.
                        <span class="badge ${data.badgeClass} ms-1">${data.analysis}</span>
                    </div>
                `;
            } else {
                display.innerHTML = '';
            }
        })
        .catch(err => console.debug('Comparison lookup:', err));
}
