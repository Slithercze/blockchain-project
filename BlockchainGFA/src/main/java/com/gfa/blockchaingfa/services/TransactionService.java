package com.gfa.blockchaingfa.services;

import com.gfa.blockchaingfa.models.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface TransactionService {

    Optional<Transaction> saveNewTransaction(Transaction newTransaction) throws IOException;

    boolean verifySignature(Transaction newTransaction) throws IOException;
    boolean hasEnoughBalance(Transaction newTransaction);

    long getBalance(String address);

    List<Transaction> calculateUTXOfromPublicKey(String publicKey);

}
