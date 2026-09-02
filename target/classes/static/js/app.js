/**
 * FarmConnect AI - Modern AgriTech Platform Interactive UI Scripts
 * SIH26033
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Auto-dismiss alert banners smoothly
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // 2. Real-time unread notification badge polling
    const notifBadge = document.getElementById('navbar-notif-badge');
    if (notifBadge) {
        fetchUnreadCount();
        setInterval(fetchUnreadCount, 25000);
    }

    // 3. Initialize Bootstrap tooltips if any
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
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
                item.classList.remove('unread-notification');
                item.classList.add('read-notification');
            }
            if (buttonElem) {
                buttonElem.outerHTML = '<span class="badge bg-light text-muted border"><i class="bi bi-check2"></i> Read</span>';
            }
            fetchUnreadCount();
        }
    });
}

function getCsrfToken() {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    return tokenMeta ? tokenMeta.getAttribute('content') : '';
}

// Global Order Total Price Calculator
function calculateOrderTotal(qtyInputId, priceInputId, totalDisplayId) {
    const qty = parseFloat(document.getElementById(qtyInputId)?.value) || 0;
    const price = parseFloat(document.getElementById(priceInputId)?.value) || 0;
    const total = Math.round(qty * price * 100) / 100;
    const display = document.getElementById(totalDisplayId);
    if (display) {
        display.textContent = '?' + total.toLocaleString('en-IN');
    }
}
