document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.querySelector('.btn-login');

    loginForm.addEventListener('submit', function() {
        // Cambiar el estilo del botón al hacer click
        loginBtn.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i> Logging in...';
        loginBtn.style.opacity = '0.7';
        loginBtn.style.pointerEvents = 'none';
    });
});