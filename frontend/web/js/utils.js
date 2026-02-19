
// Base URL for all API requests — update this if the backend is deployed elsewhere.
const API_BASE = 'http://localhost:5230';

/**
 * Authenticated fetch wrapper. Attaches the JWT Authorization header, parses
 * the JSON response, and redirects to the login page on a 401 response.
 *
 * @param {string} path - API path, e.g. '/balance'
 * @param {object} options - standard fetch options (method, headers, body, …)
 * @returns {Promise<object|null>} parsed JSON, or null if a redirect occurred
 */
function authFetch(path, options = {}) {
    const token = localStorage.getItem('token');
    const headers = {
        'Authorization': `Bearer ${token}`,
        ...options.headers
    };
    return fetch(API_BASE + path, {...options, headers})
        .then(res => {
            if (res.status === 401) {
                localStorage.removeItem('username');
                localStorage.removeItem('token');
                window.location.href = 'index.html';
                return null;
            }
            if (!res.ok) {
                throw new Error(`Server error: ${res.status}`);
            }
            return res.json();
        });
}

function clearAfter(id, ms = 4000) {
    const el = document.getElementById(id);
    if (!el) return;

    el.classList.add("fade");
    setTimeout(() => {
        el.classList.add("out");
        setTimeout(() => {
            el.textContent = "";
            el.classList.remove("out");
        }, 400);
    }, ms);
}