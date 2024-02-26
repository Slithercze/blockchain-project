package com.gfa.blockchaingfa.services.impl;

import com.gfa.blockchaingfa.exceptions.InsufficientBalanceException;
import com.gfa.blockchaingfa.exceptions.PreviousTransactionException;
import com.gfa.blockchaingfa.exceptions.SignatureVerificationException;
import com.gfa.blockchaingfa.models.Block;
import com.gfa.blockchaingfa.models.Transaction;
import com.gfa.blockchaingfa.repositories.TransactionRepository;
import com.gfa.blockchaingfa.services.TransactionService;
import lombok.Getter;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final List<Transaction> UTXO;
    @Getter
    private final List<Transaction> mempool;
    private final ApiServiceImpl apiService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository newTransactionRepository, ApiServiceImpl apiService) {
        this.transactionRepository = newTransactionRepository;
        this.UTXO = calculateUTXO();
        this.mempool = calculateMempool();
        this.apiService = apiService;
    }

    public List<Transaction> calculateUTXO() {
        List<Transaction> allMinedTxs = transactionRepository.getMinedTransactions();
        List<Transaction> utxos = new ArrayList<>();
        List<String> hashesOfSpentTx = new ArrayList<>();
        for(Transaction tx:allMinedTxs) {
            if(!tx.getPreviousTransactionId().equals("666")) {
                hashesOfSpentTx.add(tx.getPreviousTransactionId());
            }
        }
        for(Transaction tx:allMinedTxs) {
            if(!hashesOfSpentTx.contains(tx.getHash())) {
                utxos.add(tx);
            }
        }
        return utxos;
    }

    public List<Transaction> calculateUTXOfromPublicKey(String publicKey){
        List<Transaction> myUTXOS = new ArrayList<>();
        List<Transaction> utxos = calculateUTXO();
        for(Transaction tx:utxos) {
            if(tx.getToAddress().equals(publicKey)) {
                myUTXOS.add(tx);
            }
        }
        return myUTXOS;
    }

    public List<Transaction> calculateMempool() {
       return transactionRepository.getMempool();
    }

    @Override
    public Optional<Transaction> saveNewTransaction(Transaction newTransaction) throws IOException {
        if (!transactionRepository.existsById(newTransaction.getHash())) {
            if (!doesTransactionExistInUTXO(newTransaction)) {
                throw new PreviousTransactionException("Previous transaction does not exist in UTXO");
            }
            if (!verifySignature(newTransaction)) {
                throw new SignatureVerificationException("Signature verification failed");
            }
            if (!hasEnoughBalance(newTransaction)) {
                throw new InsufficientBalanceException("Insufficient balance");
            }
            if(transactionRepository.findNewTransactionByPreviousTransactionId(newTransaction.getPreviousTransactionId()).isPresent()){
                throw new PreviousTransactionException("Previous transaction already exists in UTXO");
            }
            transactionRepository.save(newTransaction);
            mempool.add(newTransaction);
            Thread thread = new Thread(() -> {
                apiService.broadcastTransaction(newTransaction);
            });
            thread.start();
            return Optional.of(newTransaction);
        }
        return Optional.empty();
    }
    public boolean doesTransactionExistInUTXO(Transaction newTransaction) {
        List<Transaction> utxos = calculateUTXO();
        for(Transaction tx:utxos) {
            if(tx.getHash().equals(newTransaction.getPreviousTransactionId())) {
                return true;
            }
        }
        return false;
    }

    public long getBalance(String address) {
        long balance = 0;
        List<Transaction> utxos = calculateUTXO();
        for(Transaction tx:utxos) {
            if(tx.getToAddress().equals(address)) {
                balance +=  tx.getAmount();
            }
        }
        return balance;
    }

    public boolean verifySignature(Transaction newTransaction) throws IOException {
        String publicKeyHex = transactionRepository.findById(newTransaction.getPreviousTransactionId()).get().getToAddress();
        String signatureHex = newTransaction.getSignature();
        String hashHex = newTransaction.calculateHash();

        byte[] publicKeyBytes = org.bouncycastle.util.encoders.Hex.decode(publicKeyHex);
        X9ECParameters curveParams = org.bouncycastle.asn1.sec.SECNamedCurves.getByName("secp256k1");
        ECDomainParameters domain = new ECDomainParameters(curveParams.getCurve(), curveParams.getG(), curveParams.getN(), curveParams.getH());
        ECPoint publicPoint = curveParams.getCurve().decodePoint(publicKeyBytes);
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(publicPoint, domain);

        ECDSASigner signer = new ECDSASigner();
        signer.init(false, publicKeyParameters);

        byte[] signatureBytes = org.bouncycastle.util.encoders.Hex.decode(signatureHex);

        // Decode the signature bytes
        ASN1InputStream asn1 = new ASN1InputStream(signatureBytes);
        ASN1Sequence sequence = (ASN1Sequence) asn1.readObject();
        BigInteger r = ((ASN1Integer) sequence.getObjectAt(0)).getValue();
        BigInteger s = ((ASN1Integer) sequence.getObjectAt(1)).getValue();

        byte[] hashBytes = Hex.decode(hashHex);

        return signer.verifySignature(hashBytes, r, s);
    }

    public List<Transaction> unspentTxsOfUser(String toAddress){
        List<Transaction> utxosOfUser = new ArrayList<>();
        List<Transaction> utxos = calculateUTXO();
        for(Transaction tx:utxos) {
            if(tx.getToAddress().equals(toAddress)) {
                utxosOfUser.add(tx);
            }
        }
        return utxosOfUser;
    }

    public boolean hasEnoughBalance(Transaction newTransaction) {
            String publicKey = transactionRepository.findById(newTransaction.getPreviousTransactionId()).get().getToAddress();
            long unspentAssets = 0L;
            List<Transaction> unspentTransactions = unspentTxsOfUser(publicKey);
            for (Transaction unspentTransaction : unspentTransactions) {
                unspentAssets += unspentTransaction.getAmount();
            }
            System.out.println("unspent assets: " + unspentAssets);
            System.out.println("new transaction amount: " + newTransaction.getAmount());
            return unspentAssets >= newTransaction.getAmount() && newTransaction.getAmount() > 0;
    }

    public boolean validateTransactions(List<Transaction> transactionsFromBlock) throws IOException {

        for (int i = 0; i < transactionsFromBlock.size(); i++) {
            if(!transactionsFromBlock.get(i).getPreviousTransactionId().equals("666")){
                if (!hasEnoughBalance(transactionsFromBlock.get(i)) ||
                        !doesTransactionExistInUTXO(transactionsFromBlock.get(i)) ||
                        !verifySignature(transactionsFromBlock.get(i))
                ) {
                    return false;
                }
            }
        }
        return true;
    }
}
