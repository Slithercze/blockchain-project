async function fetchPublicKey() {
    let token = getCookie("token");
    if (!token || token === 'null' || token === '') {
        return;
    }

    try {
        let response = await fetch('/getPublicKey');
        if (!response.ok) {
            throw new Error('HTTP error ' + response.status);
        }
        let publicKey = await response.text();
        document.querySelector('.keyInput').textContent = publicKey;

    } catch (error) {
        console.error('Error:', error);
    }
}
async function fetchUsername() {
    let token = getCookie("token");
    if (!token || token === 'null' || token === '') {
        return;
    }

    try {
        let response = await fetch('/getUsername', {
            headers: {
                'Cookie': `token=${token}`
            }
        });
        if (!response.ok) {
            throw new Error('HTTP error ' + response.status);
        }
        let username = await response.text();
        return username;
    } catch (error) {
        console.error('Error:', error);
    }
}

async function fetchBalance() {
    let token = getCookie("token");
    if (!token || token === 'null' || token === '') {
        return;
    }

    try {
        let response = await fetch('/getBalance', {
            headers: {
                'Cookie': `token=${token}`
            }
        });
        if (!response.ok) {
            throw new Error('HTTP error ' + response.status);
        }
        let balance = await response.text();
        return balance;
    } catch (error) {
        console.error('Error:', error);
    }
}

async function updateUserInfo() {
    let token = getCookie("token");
    if (!token || token === 'null' || token === '') {
        document.getElementById('login-button').style.display = 'inline';
        document.getElementById('register-button').style.display = 'inline';
        document.getElementById('logout-button').style.display = 'none';
        document.getElementById('username').style.display = 'none';
        document.getElementById('balance').style.display = 'none';
        return;
    }

    let username = await fetchUsername();
    let balance = await fetchBalance();

    if (username && balance) {
        document.getElementById('login-button').style.display = 'none';
        document.getElementById('register-button').style.display = 'none';
        document.getElementById('logout-button').style.display = 'inline';
        document.getElementById('username').style.display = 'inline';
        document.getElementById('balance').style.display = 'inline';
        document.getElementById('username').textContent = `User: ${username}`;
        document.getElementById('balance').textContent = `Balance: ${balance}`;
    }
}


document.addEventListener('DOMContentLoaded', () => {
    let token = getCookie("token");
    let genKeyButton = document.querySelector('.genKey');
    let sendTxButton = document.querySelector('.sendTx');
    let viewTxButton = document.querySelector('.viewTxBtn');
    let loggedOutMessage = document.getElementById('logged-out-message');


    if (!token || token === 'null' || token === '') {
        genKeyButton.disabled = true;
        sendTxButton.disabled = true;
        viewTxButton.disabled = true;
        genKeyButton.title = "Please log in to generate keys";
        sendTxButton.title = "Please log in to sign and send";
        viewTxButton.title = "Please log in to view transactions";
        loggedOutMessage.style.display = 'block';
    } else {
        genKeyButton.disabled = false;
        sendTxButton.disabled = false;
        viewTxButton.disabled = false;
        genKeyButton.title = "";
        sendTxButton.title = "";
        viewTxButton.title = "";
        loggedOutMessage.style.display = 'none';
        fetchPublicKey();
        updateUserInfo();
    }
});

function getCookie(name) {
    let cookieArr = document.cookie.split(";");

    for (let i = 0; i < cookieArr.length; i++) {
        let cookiePair = cookieArr[i].split("=");

        let key = cookiePair[0].trim();
        let value = cookiePair.length > 1 ? cookiePair[1].trim() : '';

        if (name === key) {
            return decodeURIComponent(value);
        }
    }
    return null;
}

document.querySelector('.viewTxBtn').addEventListener('click', function(event) {
    const dropdown = document.getElementById("transactionDropdown");

    if (!dropdown.classList.contains("open")) {
        fetchTransactions();
        dropdown.classList.add("open");
    } else {
        dropdown.classList.remove("open");
    }

    event.stopPropagation();
});

document.addEventListener('click', handleClickOutside);

function handleClickOutside(event) {
    const dropdown = document.getElementById("transactionDropdown");

    if (!dropdown.contains(event.target) && dropdown.classList.contains("open")) {
        dropdown.classList.remove("open");
    }
}

function fetchTransactions() {
    fetch("http://localhost:8080/getTransactions")
        .then(response => response.json())
        .then(transactions => {
            const dropdown = document.getElementById("transactionDropdown");
            dropdown.innerHTML = ""; // Clear previous transactions

            transactions.forEach(tx => {
                const div = document.createElement("div");
                div.className = "transaction-item";

                const txDetails = `
                    <span class="copy-hash">Hash: <span class="hashBlue">${tx.hash}</span></span><br>
                    Amount: ${tx.amount}<br>
                    Timestamp: ${new Date(tx.timestamp).toLocaleString()}<br>
                    Previous Tx Id: ${tx.previousTransactionId}<br>
                    Signature: ${tx.signature}<br>
                `;

                div.innerHTML = txDetails;
                dropdown.appendChild(div);

                // Add an event listener to copy the hash to clipboard
                div.querySelector('.copy-hash').addEventListener('click', function() {
                    copyTextToClipboard(tx.hash);
                });
            });

            dropdown.style.display = "block";
        })
        .catch(error => {
            console.error('Error fetching transactions:', error);
        });
}

function copyTextToClipboard(text) {
    const textArea = document.createElement("textarea");
    textArea.style.position = 'fixed';
    textArea.style.top = '0';
    textArea.style.left = '0';
    textArea.style.width = '2em';
    textArea.style.height = '2em';
    textArea.style.padding = '0';
    textArea.style.border = 'none';
    textArea.style.outline = 'none';
    textArea.style.boxShadow = 'none';
    textArea.style.background = 'transparent';
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();

    const notification = document.getElementById('copy-notification');
    notification.style.opacity = '1';
    setTimeout(() => {
        notification.style.opacity = '0';
    }, 2000);
    try {
        document.execCommand('copy');
    } catch (err) {
        console.error('Unable to copy text to clipboard', err);
    }

    document.body.removeChild(textArea);
}
