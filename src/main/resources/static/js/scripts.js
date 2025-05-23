// Scripts personnalisés pour l'application

// Auto-disparition des alertes après 5 secondes
document.addEventListener('DOMContentLoaded', function() {
    // Initialiser tous les datepickers
    if (typeof flatpickr !== 'undefined') {
        flatpickr(".datepicker", {
            locale: "fr",
            dateFormat: "Y-m-d",
            altInput: true,
            altFormat: "d/m/Y",
            minDate: "today"
        });
    }

    // Gérer la disparition automatique des alertes
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            if (alert && typeof bootstrap !== 'undefined') {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000);
    });

    // Activer les tooltips Bootstrap
    if (typeof bootstrap !== 'undefined' && typeof bootstrap.Tooltip !== 'undefined') {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }

    // Appliquer des couleurs aux priorités
    const priorityBadges = document.querySelectorAll('.badge[data-priority]');
    priorityBadges.forEach(function(badge) {
        const priority = badge.getAttribute('data-priority');
        badge.classList.add(`priority-${priority}`);
    });
});

// Confirmation pour les actions sensibles
function confirmAction(message) {
    return confirm(message || 'Êtes-vous sûr de vouloir effectuer cette action?');
}