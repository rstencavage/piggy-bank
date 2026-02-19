const username = localStorage.getItem("username");
const token = localStorage.getItem("token");

if (!username || !token) {
    window.location.href = "index.html";
}

window.addEventListener('DOMContentLoaded', function () {
    loadHistory();
});

function loadHistory() {
    fetch(`http://localhost:5230/history`, {
        headers: {'Authorization': `Bearer ${token}`}
    })
        .then(response => response.json())
        .then(data => {
            const historyContent = document.getElementById('historyContent');

            if (!data.success || !data.transactions || data.transactions.length === 0) {
                historyContent.innerHTML =
                    '<p id="historyMessage">No transaction history found.</p>';
                return;
            }

            historyContent.innerHTML = '';

            // Newest first
            const transactions = data.transactions.slice().reverse();

            transactions.forEach(tx => {const isIncoming = tx.type === "DEPOSIT" || tx.type === "TRANSFER_IN";

                const amountText =
                    (isIncoming ? "+" : "-") +
                    Number(tx.amount).toLocaleString("en-US", {
                        style: "currency",
                        currency: "USD"
                    });

                const div = document.createElement('div');
                div.className = 'transaction';

                div.innerHTML = `
                    <div class="transaction-header">
                        <span class="transaction-type">${tx.type}</span>
                        <span class="transaction-amount ${isIncoming ? 'positive' : 'negative'}">
                            ${amountText}
                        </span>
                    </div>
                    <div class="transaction-details">${tx.time}</div>
                `;

                historyContent.appendChild(div);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('historyContent').innerHTML =
                '<p id="historyMessage">Error loading history</p>';
        });
}