// API Configuration

const API_URLS = {
    auth: `https://quiz-auth-service.onrender.com/auth`,
    question: `https://quiz-question-service.onrender.com/question`,
    quiz: `https://quiz-quiz-service.onrender.com/quiz`
};

// Auth Helper Functions
function getToken() {
    return localStorage.getItem('token');
}

function getUserRole() {
    return localStorage.getItem('role');
}

function getUserInfo() {
    const userInfo = localStorage.getItem('userInfo');
    return userInfo ? JSON.parse(userInfo) : null;
}

function setAuthData(token, role, userInfo) {
    localStorage.setItem('token', token);
    localStorage.setItem('role', role);
    localStorage.setItem('userInfo', JSON.stringify(userInfo));
}

function clearAuthData() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userInfo');
}

function isAuthenticated() {
    return !!getToken();
}

function isAdmin() {
    return getUserRole() === 'ADMIN';
}

// API Request Helper
async function apiRequest(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers
    };

    try {
        const response = await fetch(url, {
            ...options,
            headers
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Request failed');
        }

        return data;
    } catch (error) {
        console.error('API Request Error:', error);
        throw error;
    }
}

// Update Navbar based on auth state
function updateNavbar() {
    const navAuth = document.getElementById('nav-auth');
    const navLogout = document.getElementById('nav-logout');
    const navAdmin = document.getElementById('nav-admin');
    const navQuizzes = document.getElementById('nav-quizzes');

    if (isAuthenticated()) {
        if (navAuth) navAuth.style.display = 'none';
        if (navLogout) navLogout.style.display = 'block';

        if (isAdmin()) {
            if (navAdmin) navAdmin.style.display = 'block';
        } else {
            if (navAdmin) navAdmin.style.display = 'none';
        }

        if (navQuizzes) navQuizzes.style.display = 'block';
    } else {
        if (navAuth) navAuth.style.display = 'block';
        if (navLogout) navLogout.style.display = 'none';
        if (navAdmin) navAdmin.style.display = 'none';
    }

    // Logout handler
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            clearAuthData();
            window.location.href = 'login.html';
        });
    }
}

// Check if user has required role
function requireAuth(requiredRole = null) {
    if (!isAuthenticated()) {
        window.location.href = 'login.html';
        return false;
    }

    if (requiredRole && getUserRole() !== requiredRole) {
        alert('You do not have permission to access this page');
        window.location.href = 'index.html';
        return false;
    }

    return true;
}

// Show messages
function showMessage(elementId, message, type = 'success') {
    const element = document.getElementById(elementId);
    if (element) {
        element.className = `alert alert-${type}`;
        element.textContent = message;
        element.style.display = 'block';

        setTimeout(() => {
            element.style.display = 'none';
        }, 5000);
    }
}

// Show loading spinner
function showLoading(show = true) {
    const loading = document.getElementById('loading');
    if (loading) {
        loading.style.display = show ? 'block' : 'none';
    }
}
