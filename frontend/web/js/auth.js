
document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("login-form").addEventListener("submit", login);
    document.getElementById("register-form").addEventListener("submit", register);
});


/**
 * Handles user login by sending credentials to backend API.
 */
function login(event) {
    event.preventDefault();

    // Get values from the input fields
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    // POST request from backend
    fetch('http://localhost:5230/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username: username, password: password})
    })
        .then(response => response.json())
        .then(data => {
            // Display result message
            const message = document.getElementById('loginMessage');
            message.textContent = data.message;
            clearAfter("loginMessage", 4000);

            if(data.success){
                localStorage.setItem("username", username);
                window.location.href = "dashboard.html";
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const msg = document.getElementById('loginMessage');
            msg.textContent = 'Connection error';
            clearAfter("loginMessage", 4000);
        });
}

/**
 * Handles user registration by sending credentials to backend API.
 */
function register(event) {
    event.preventDefault();

    // Get values from the input fields
    const username = document.getElementById('registerUsername').value;
    const password = document.getElementById('registerPassword').value;

    // POST request from backend
    fetch('http://localhost:5230/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({username: username, password: password})
    })
        .then(response => response.json())
        .then(data => {
            // Display result message
            const message = document.getElementById('registerMessage');
            message.textContent = data.message;
            clearAfter("registerMessage", 4000);

            // Clear fields if successful
            if (data.success) {
                document.getElementById('registerUsername').value = '';
                document.getElementById('registerPassword').value = '';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const msg = document.getElementById('registerMessage');
            msg.textContent = 'Connection error';
            clearAfter("registerMessage", 4000);
        });
}