package com.gfa.blockchaingfa.exceptions;

public class SignatureVerificationException extends RuntimeException {
    public SignatureVerificationException(String message) {
        super(message);
    }
}
