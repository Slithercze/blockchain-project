

const ec = new elliptic.ec('secp256k1');

let key;
let publicKey;
let privateKey;
let privKeyInForm;

const btn = document.querySelector('.genKey');

btn.addEventListener('click', () => {
    key = ec.genKeyPair();
    publicKey = key.getPublic('hex');
    privateKey = key.getPrivate('hex');

    document.querySelector('.privateKey1').value = privateKey;
    const privKeyElement = document.querySelector('.privKey');
    document.querySelector('.keyInput').value = publicKey;
    privKeyElement.querySelector("span").textContent = privateKey;
    privKeyElement.classList.remove("hide");

    fetch("/updateKeys", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            publicKey: publicKey,
        }),
    })
        .then(response => {
            if (response.status === 401) {
                throw new Error('User not authenticated');
            }
            return response.json();
        })
        .then(data => console.log(data))
        .catch(error => console.error('Error:', error.message));

});

const formTX = document.querySelector('.formTx');
formTX.addEventListener('submit', async e => {
    e.preventDefault();
    // Generate key inside the submit event
    const formData = new FormData(formTX);
    const dataEntries = Object.fromEntries(formData);
    privKeyInForm = ec.keyFromPrivate(dataEntries.privateKey);
    publicKey = privKeyInForm.getPublic('hex');
    privateKey = privKeyInForm.getPrivate('hex');

    let trimmedPubKey = dataEntries.toAddress
    dataEntries.toAddress = trimmedPubKey.trim();
    delete dataEntries.privateKey;

    dataEntries.timestamp = Math.floor(Date.now());
    dataEntries.amount = 1;

    async function calculateHash() {
        const jsonString = dataEntries.toAddress + dataEntries.previousTransactionId + dataEntries.amount + dataEntries.timestamp;
        const utf8 = new TextEncoder().encode(jsonString);
        return crypto.subtle.digest('SHA-256', utf8).then((hashBuffer) => {
            const hashArray = Array.from(new Uint8Array(hashBuffer));
            return hashArray
                .map((bytes) => bytes.toString(16).padStart(2, '0'))
                .join('');
        });
    }

    let hashTx = await calculateHash();

    function signTransaction() { //signingKey je privatni klíč
        return new Promise(async (resolve) => {
            const sig = privKeyInForm.sign(hashTx, "base64");
            const signatureDER = sig.toDER("hex");
            resolve(signatureDER);
        });
    }

    const signatureDER = await signTransaction();




    dataEntries.hash = hashTx;
    dataEntries.signature = signatureDER;

    console.log("logged JSON DATA before fetch", dataEntries)

    fetch("api/newtransaction", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(dataEntries)
    })
        .then(response => {
            console.log(response)
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text)
                });
            }
            return response.text().then(text => text ? JSON.parse(text) : {});
        })
        .then(data => {
            document.querySelector(".message").textContent = "Transaction added to the pool";
            console.log(data);
            document.querySelector('.formTx').reset();
        })
        .catch(error => {
            console.error('Error:', error);
            document.querySelector(".message").textContent = error.message;
        });
});
