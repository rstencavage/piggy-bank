const token = localStorage.getItem("token");

if (!token) window.location.href = "index.html";

const username = localStorage.getItem("username");

window.addEventListener('DOMContentLoaded', function () {
    document.getElementById('username').textContent = username;
    getBalance();
    recentHistory();

    document.getElementById("deposit-form").addEventListener("submit", deposit);
    document.getElementById("withdraw-form").addEventListener("submit", withdraw);
    document.getElementById("transfer-form").addEventListener("submit", transfer);
});

function updateBalance(el, newText) {
    el.textContent = newText;
    el.classList.remove("flash"); // reset if spammed
    void el.offsetWidth;          // force reflow
    el.classList.add("flash");
}

function getBalance() {
    fetch("http://localhost:5230/balance", {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const balanceDisplay = document.getElementById('balanceDisplay');

            if (!data.success) {
                balanceDisplay.textContent = '0.00';
                return;
            }

            updateBalance(
                balanceDisplay,
                data.balance.toLocaleString("en-US", {
                    style: "currency",
                    currency: "USD"
                })
            );
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('balanceDisplay').textContent = '0.00';
        });
}

function deposit(event) {
    event.preventDefault();

    const amount = document.getElementById('depositAmount').value;

    fetch("http://localhost:5230/deposit", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            amount: amount
        })
    })
        .then(response => response.json())
        .then(data => {
            const message = document.getElementById('depositMessage');
            message.textContent = data.message;
            clearAfter("depositMessage", 4000);

            if (data.success) {
                getBalance();
                document.getElementById('depositAmount').value = '';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const msg = document.getElementById('depositMessage');
            msg.textContent = 'Connection error';
            clearAfter("depositMessage", 4000);
        });
}

function withdraw(event) {
    event.preventDefault();

    const amount = document.getElementById('withdrawAmount').value;

    fetch("http://localhost:5230/withdraw", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            amount: amount
        })
    })
        .then(response => response.json())
        .then(data => {
            const message = document.getElementById('withdrawMessage');
            message.textContent = data.message;
            clearAfter("withdrawMessage", 4000);

            if (data.success) {
                getBalance();
                document.getElementById('withdrawAmount').value = '';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const msg = document.getElementById('withdrawMessage');
            msg.textContent = 'Connection error';
            clearAfter("withdrawMessage", 4000);
        });
}

function transfer(event) {
    event.preventDefault();
    const toUser = document.getElementById('transferTo').value;
    const amount = document.getElementById('transferAmount').value;

    fetch("http://localhost:5230/transfer", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            toUser: toUser,
            amount: amount
        })
    })
        .then(response => response.json())
        .then(data => {
            const message = document.getElementById('transferMessage');
            message.textContent = data.message;
            clearAfter("transferMessage", 4000);

            if (data.success) {
                getBalance();
                document.getElementById('transferAmount').value = '';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const msg = document.getElementById('transferMessage');
            msg.textContent = 'Connection error';
            clearAfter("transferMessage", 4000);
        });
}

function recentHistory() {
    fetch("http://localhost:5230/history", {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const historyDiv = document.getElementById('recentHistory');

            if (!data.success || !data.transactions || data.transactions.length === 0) {
                historyDiv.innerHTML =
                    '<p id="historyPreviewMessage">No transactions yet</p>';
                return;
            }

            // Take last 3 transactions (newest first)
            const recent = data.transactions.slice(-3).reverse();

            historyDiv.innerHTML = '';

            recent.forEach(tx => {
                const isIncoming = tx.type === "DEPOSIT" || tx.type === "TRANSFER_IN";

                const amountText =
                    (isIncoming ? "+" : "-") +
                    Number(tx.amount).toLocaleString("en-US", {
                        style: "currency",
                        currency: "USD"
                    });

                const div = document.createElement('div');
                div.className = 'mini-transaction';

                div.innerHTML = `
                    <div class="mini-transaction-amount ${isIncoming ? 'positive' : 'negative'}">
                        ${amountText}
                    </div>
                    <div style="font-size: 0.85em; color: rgba(255,255,255,0.6); margin-top: 5px;">
                        ${tx.type}
                    </div>
                `;

                historyDiv.appendChild(div);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('recentHistory').innerHTML =
                '<p id="historyPreviewMessage">Error loading history</p>';
        });
}

function logout() {
    localStorage.removeItem("username");
    window.location.href = "index.html";
}