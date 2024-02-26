package com.gfa.blockchaingfa.controllers;

import com.gfa.blockchaingfa.services.PeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PeerController {
    private final PeerService peerService;

    @Autowired
    public PeerController(PeerService peerService) {
        this.peerService = peerService;
    }

    @GetMapping("/peers")
    public ResponseEntity<?> getTenPeers() {
        return ResponseEntity.ok().body(peerService.findTenPeers());
    }
}
