package com.gfa.blockchaingfa.handlers;

import com.gfa.blockchaingfa.exceptions.InsufficientBalanceException;
import com.gfa.blockchaingfa.exceptions.SignatureVerificationException;
import com.gfa.blockchaingfa.exceptions.PreviousTransactionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SignatureVerificationException.class)
    public ResponseEntity<?> handleSignatureVerificationException(SignatureVerificationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<?> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance");
    }

    @ExceptionHandler(PreviousTransactionException.class)
    public ResponseEntity<?> handlePreviousTransactionException(PreviousTransactionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
