package com.gfa.blockchaingfa.exceptions;

public class PreviousTransactionException extends RuntimeException{
    public PreviousTransactionException(String message) {
        super(message);
    }
}
