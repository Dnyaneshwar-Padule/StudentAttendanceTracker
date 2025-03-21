/**
 * Student Attendance Management System
 * Common JavaScript functions for the application
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize form validation for all forms
    initFormValidation();
    
    // Initialize tooltips
    initTooltips();
    
    // Auto-dismiss alerts after 5 seconds
    autoDismissAlerts();
    
    // Initialize dropdown behavior
    initDropdowns();
});

/**
 * Initialize custom form validation
 */
function initFormValidation() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                
                // Find first invalid field and focus it
                const invalidField = form.querySelector(':invalid');
                if (invalidField) {
                    invalidField.focus();
                }
            }
            
            form.classList.add('was-validated');
        });
    });
    
    // Password confirmation validation
    const passwordFields = document.querySelectorAll('input[type="password"][id="password"]');
    const confirmFields = document.querySelectorAll('input[type="password"][id="confirmPassword"]');
    
    if (passwordFields.length > 0 && confirmFields.length > 0) {
        confirmFields[0].addEventListener('input', function() {
            if (this.value !== passwordFields[0].value) {
                this.setCustomValidity('Passwords do not match');
            } else {
                this.setCustomValidity('');
            }
        });
        
        passwordFields[0].addEventListener('input', function() {
            if (confirmFields[0].value !== '' && confirmFields[0].value !== this.value) {
                confirmFields[0].setCustomValidity('Passwords do not match');
            } else {
                confirmFields[0].setCustomValidity('');
            }
        });
    }
}

/**
 * Initialize Bootstrap tooltips
 */
function initTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

/**
 * Auto-dismiss alert messages after 5 seconds
 */
function autoDismissAlerts() {
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
}

/**
 * Initialize dropdown behavior for cascading filter selects
 */
function initDropdowns() {
    // Example: if department dropdown exists, set up class dropdown to be populated
    const departmentDropdown = document.getElementById('departmentId');
    const classDropdown = document.getElementById('classId');
    
    if (departmentDropdown && classDropdown) {
        // This would typically be handled server-side with form submission
        // But we can handle it with JavaScript for a more dynamic experience
        departmentDropdown.addEventListener('change', function() {
            // This is just a placeholder - in a real app this would fetch classes for the department
            console.log('Department selected: ' + this.value);
        });
    }
}

/**
 * Toggle password visibility
 * @param {string} inputId - The ID of the password input field
 * @param {string} toggleId - The ID of the toggle button/icon
 */
function togglePasswordVisibility(inputId, toggleId) {
    const passwordInput = document.getElementById(inputId);
    const toggleButton = document.getElementById(toggleId);
    
    if (passwordInput && toggleButton) {
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            toggleButton.innerHTML = '<i class="fas fa-eye-slash"></i>';
        } else {
            passwordInput.type = 'password';
            toggleButton.innerHTML = '<i class="fas fa-eye"></i>';
        }
    }
}

/**
 * Format dates in user's preferred format
 * @param {string} dateString - ISO date string to format
 * @returns {string} Formatted date string
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString();
}

/**
 * Calculate the attendance percentage
 * @param {number} present - Number of present days
 * @param {number} total - Total number of days
 * @returns {string} Formatted percentage
 */
function calculateAttendancePercentage(present, total) {
    if (total === 0) return "0.00%";
    const percentage = (present / total) * 100;
    return percentage.toFixed(2) + "%";
}

/**
 * Get attendance status badge class
 * @param {string} status - Attendance status (Present, Absent, Leave)
 * @returns {string} CSS class for the badge
 */
function getAttendanceStatusClass(status) {
    switch(status) {
        case 'Present':
            return 'badge-success';
        case 'Absent':
            return 'badge-danger';
        case 'Leave':
            return 'badge-warning';
        default:
            return 'badge-secondary';
    }
}

/**
 * Get badge class for attendance percentage
 * @param {number} percentage - Attendance percentage
 * @returns {string} CSS class for the badge
 */
function getAttendancePercentageBadgeClass(percentage) {
    if (percentage >= 75) {
        return 'badge-success';
    } else if (percentage >= 60) {
        return 'badge-warning';
    } else {
        return 'badge-danger';
    }
}
