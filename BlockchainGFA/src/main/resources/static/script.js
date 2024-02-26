const form = document.querySelector("form");
form.addEventListener("submit", continuousMining);
const audioPlayer = document.getElementById('audio-player');

let stopMine = true;
let allBlocks = [];

await updateBlocks();

function showTxInfoOnBlock(event) {
  event.preventDefault();
  const button = event.target;
  const blockElement = button.closest(".block");
  const transactionElement = blockElement.querySelectorAll(".transactionField");
  for (const transaction of transactionElement) {
    if (transaction.classList.contains("hide")) {
      transaction.classList.remove("hide");
    } else {
      transaction.classList.add("hide");
    }
  }
  if(button.classList.contains("up")) {
    button.classList.remove("up");
    button.classList.add("down");
  } else {
    button.classList.remove("down");
    button.classList.add("up");
  }
}

function handleStopButtonClick() {
  stopMine = true;
  audioPlayer.pause();
  audioPlayer.currentTime = 0;
}

function Transaction(hash, amount, toAddress, timestamp, previousTransactionId, signature) {
  this.hash = hash;
  this.amount = amount;
  this.toAddress = toAddress;
  this.timestamp = timestamp;
  this.previousTransactionId = previousTransactionId;
  this.signature = signature;
  this.setHash = async function () {
    const stringForHash = this.previousTransactionId + this.amount + this.toAddress + this.timestamp;
    this.hash = await calculateHash(stringForHash);
  }
}

function BlockData(data) {
  this.hash = null;
  this.transactions = [];
  this.data = data;
  this.timestamp = Math.floor(Date.now() / 1000);
  this.setHash = async function () {
    const obj = {
      transactions: this.transactions,
      data: this.data,
      timestamp: this.timestamp,
    };
    this.hash = await calculateHash(JSON.stringify(obj));
  };
}

function Block(blockData) {
  this.hash = null;
  this.blockData = blockData;
  this.previousHash = null;
  this.timestamp = Math.floor(Date.now() / 1000);
  this.nonce = 0;
  this.setHash = async function () {
    const jsonBlockData = JSON.stringify(this.blockData);
    const stringForHash =
      this.previousHash + this.timestamp + jsonBlockData + this.nonce;
    this.hash = await calculateHash(stringForHash);
  };
}

async function calculateHash(stringForHash) {
  //const jsonString = JSON.stringify(obj);
  const encoder = new TextEncoder();
  const data = encoder.encode(stringForHash);
  const hashBuffer = await crypto.subtle.digest("SHA-256", data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
  return hashHex;
}

async function continuousMining(event) {
  event.preventDefault();
  audioPlayer.play();

  if(!document.querySelector(".privKey").classList.contains("hide")) {
    document.querySelector(".privKey").classList.add("hide")
  }

  const buttonElement = event.target.querySelectorAll("button")[1];
  if(stopMine) {
    buttonElement.textContent = "Stop Mining";
    stopMine = false;
  } else {
    buttonElement.textContent = "Start Mining";
    stopMine = true;
    audioPlayer.pause();
    audioPlayer.currentTime = 0;
    await updateBlocks();
    return;
  }

  const minerDivElement = document.querySelector(".miner");
  minerDivElement.classList.remove("hide");

  const miningStatusElement = minerDivElement.querySelector(".miningStatus");
  const hashesPerSecondElement = minerDivElement.querySelector(".hashesPerSecond");
  const miningTimeElement = minerDivElement.querySelector(".miningTime")
  const startTime = Math.floor(Date.now() / 1000);

  let minerStopper = false;
  const eventSource = new EventSource('/api/updates');
  eventSource.addEventListener('minerStopperUpdate', (event) => {
    minerStopper = JSON.parse(event.data);
  });
  let successfullyMinedBlocks = 0;
  let testCount = 1;
  while(!stopMine) {
    const work = await getWork();
    miningStatusElement.textContent = "Mining the block with height " + work["height"];
    console.log(work);
    const difficulty = work["difficulty"];
    const prefix = "0".repeat(difficulty);
    const coinBasePubKey = event.target.querySelector("textarea").value;

    const block = await buildBlockFromWork(work, coinBasePubKey);

    let lastNonce = 0;
    let lastTime = Math.floor(Date.now() / 1000);

    while (block.hash.substring(0, difficulty) !== prefix && !minerStopper && !stopMine) {
      block.nonce++;
      block.timestamp = Math.floor(Date.now() / 1000);
      await block.setHash();

      if (block.timestamp !== lastTime) {
        hashesPerSecondElement.textContent = String(block.nonce - lastNonce) + " nonces per second";
        miningTimeElement.textContent = formatTime(block.timestamp - startTime);
        lastNonce = block.nonce;
      }

      lastTime = block.timestamp;
    }
    if (minerStopper === true) {
      minerStopper = false;
      console.log("New work received");
    } else if(stopMine === true) {
      miningStatusElement.textContent = "You stopped with " + successfullyMinedBlocks + " blocks successfully mined.";
    } else if(block.hash.substring(0, difficulty) === prefix) {
      console.log("new block mined")
      successfullyMinedBlocks++;
      await postMinedBlock(block, work["height"]);
    }
    testCount++;
    await updateBlocks();
  }
  eventSource.close();
  hashesPerSecondElement.textContent = "";
  miningStatusElement.textContent = "You stopped with " + successfullyMinedBlocks + " blocks successfully mined."
}

async function buildBlockFromWork(work, coinBasePubKey) {
  const blockData = new BlockData( "Block with height " + work["height"]);
  const sortedTransactions = work["txList"].sort((tx1, tx2) => tx2.timestamp - tx1.timestamp);
  if(sortedTransactions !== null) {
    for (const tx of sortedTransactions) {
      const transaction = new Transaction(tx.hash, tx.amount, tx.toAddress, tx.timestamp, tx.previousTransactionId, tx.signature);
      blockData.transactions.push(transaction);
    }
  }
  const coinBaseTx = new Transaction("0",1, coinBasePubKey,Math.floor(Date.now()),"666","666");
  await coinBaseTx.setHash();
  blockData.transactions.unshift(coinBaseTx);
  await blockData.setHash();
  const block = new Block(blockData);
  block.timestamp = Math.floor(Date.now());
  block.previousHash = work["previousHash"];
  await block.setHash();
  return block;
}

function createStopButton() {
  const stopButtonElement = document.createElement("button");
  stopButtonElement.classList.add("stopButton");
  stopButtonElement.addEventListener("click",handleStopButtonClick);
  return stopButtonElement;
}

async function postMinedBlock(block, height) {
  console.log(JSON.stringify(block));
  try {
    const response = await fetch("/api/blocks", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "height": height
      },
      body: JSON.stringify(block),
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      throw new Error(`Failed to post mined block: ${errorMessage}`);
    }
  } catch (error) {
    console.error(error);
  }
}

async function getWork() {
  try {
    const response = await fetch('/api/blocks/work');
    if (!response.ok) {
      throw new Error('Failed to fetch work data');
    }
    const data = await response.json();
    return data;
  } catch (error) {
    console.error(error);
  }
}

async function updateBlocks() {
  const chainElement = document.querySelector(".chain");
  const blockTemplateElement = document.querySelector(".template.block");
  const numberOfDisplayedBlocks = document.querySelectorAll(".block").length - 1; //-1 because of the template
  allBlocks = await getBlocksFomHeight(numberOfDisplayedBlocks);

  for (let block of allBlocks) {
    chainElement.prepend(createBlockElement(block, blockTemplateElement));
  }
}

async function getBlocksFomHeight(height) {
  const response = await fetch('/api/blocks',{
    headers: {
      "Content-Type": "application/json",
      "height": height
    },
  });
  if (!response.ok) {
    throw new Error('Failed to fetch Blockchain');
  }
  return response.json();
}

function createTransactionElement(transaction, transactionTemplate) {
  const transactionElement = transactionTemplate.cloneNode(true);
  transactionElement.classList.remove("template");
  transactionElement.querySelector(".txId").textContent = transaction.hash;
  transactionElement.querySelector(".previousTxId").textContent = transaction.previousTransactionId;
  transactionElement.querySelector(".txToAddress").textContent = transaction.toAddress.slice(0,30) + "...";
  transactionElement.querySelector(".txTimestamp").textContent = getReadableTime(transaction.timestamp / 1000);;
  return transactionElement;
}

function createBlockElement(block, templateElement) {
  const blockElement = templateElement.cloneNode(true);
  blockElement.classList.remove('template');

  blockElement.querySelector(".hash-value").textContent = block.hash;
  blockElement.querySelector(".previousHash-value").textContent = block.previousHash;
  blockElement.querySelector(".timestamp-value").textContent = getReadableTime(block.timestamp);
  blockElement.querySelector(".nonce-value").textContent = block.nonce;
  if(block.blockData === null) {
    blockElement.querySelector(".block-data").textContent = "Genesis";
  } else {
    blockElement.querySelector(".block-data").textContent = block.blockData.data;
    const transactionTemplate = blockElement.querySelector(".transactionField");
    for (const transaction of block.blockData.transactions) {
      blockElement.appendChild(createTransactionElement(transaction, transactionTemplate))
    }
  }
  const showTxInfoButton = blockElement.querySelector(".blockInfo");
  showTxInfoButton.addEventListener("click", showTxInfoOnBlock)
  return blockElement;
}

function displayMinedBlock(block) {
  const chainElement = document.querySelector(".chain");
  const templateElement = document.querySelector(".template")
  chainElement.prepend(createBlockElement(block,templateElement));
}

function getReadableTime(timestampSeconds) {
  const date = new Date(timestampSeconds * 1000);

  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();
  const day = date.getDate();
  const month = date.getMonth() + 1; // Note: months are 0-indexed, so we add 1
  const year = date.getFullYear();

  const readableFormat = `${hours}:${formatTwoDigits(minutes)}:${formatTwoDigits(seconds)}, ${formatTwoDigits(day)}/${formatTwoDigits(month)}/${year}`;

  return readableFormat;
}

function formatTwoDigits(number) {
  return number.toString().padStart(2, '0');
}

function formatTime(seconds) {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;

  const formattedHours = hours.toString().padStart(2, '0');
  const formattedMinutes = minutes.toString().padStart(2, '0');
  const formattedSeconds = remainingSeconds.toString().padStart(2, '0');

  return `${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
}
