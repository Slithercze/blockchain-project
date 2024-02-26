package com.gfa.blockchaingfa.controllers;

import com.gfa.blockchaingfa.models.Transaction;
import com.gfa.blockchaingfa.services.impl.TransactionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("/api/newtransaction")
@RestController
public class TransactionController {
    private final TransactionServiceImpl mempoolService;

    @Autowired
    public TransactionController(TransactionServiceImpl mempoolService) {
        this.mempoolService = mempoolService;
    }

    @PostMapping("")
    public ResponseEntity<?> receiveNewTransaction(@RequestBody Transaction transaction) throws IOException {
        return ResponseEntity.ok().body(mempoolService.saveNewTransaction(transaction));
    }

}
