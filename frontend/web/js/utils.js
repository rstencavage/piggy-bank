
const API_BASE = 'http://localhost:5230';

/**
 * Fetch wrapper that attaches the JWT Authorization header.
 * Redirects to login automatically on 401 Unauthorized.
 */
async function authFetch(path, options = {}) {
    const token = localStorage.getItem("token");
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    const response = await fetch(API_BASE + path, { ...options, headers });
    if (response.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        window.location.href = "index.html";
        throw new Error("Unauthorized");
    }
    return response;
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