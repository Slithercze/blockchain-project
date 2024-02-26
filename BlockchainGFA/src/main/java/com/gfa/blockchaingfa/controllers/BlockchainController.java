package com.gfa.blockchaingfa.controllers;

import com.gfa.blockchaingfa.models.Block;
import com.gfa.blockchaingfa.services.BlockService;
import com.gfa.blockchaingfa.services.PeerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class BlockchainController {
    private final BlockService blockService;
    private final PeerService peerService;

    @Autowired
    public BlockchainController(BlockService blockService, PeerService peerService) {
        this.blockService = blockService;
        this.peerService = peerService;
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mineBlock(@RequestHeader(required = false) Integer port, HttpServletRequest ipReq) {
        peerService.savePeer(port, ipReq);
        return ResponseEntity.ok().header("height", String.valueOf(blockService.getBlockchainHeight()))
                .body(blockService.mine());
    }

    @GetMapping("/blocks/{hash}")
    public ResponseEntity<?> getBlockByHash(@PathVariable String hash, @RequestHeader(required = false) Integer port,
                                            HttpServletRequest ipReq) {
        peerService.savePeer(port, ipReq);
        try {
            return ResponseEntity.ok().body(blockService.findByHash(hash));
        } catch (Exception exception) {
            return ResponseEntity.status(404).body(exception.getMessage());
        }
    }

    @PostMapping("/blocks")
    public ResponseEntity<?> receiveBlock(@RequestBody Block block, @RequestHeader(required = false) Integer port,
                                          HttpServletRequest ipReq, @RequestHeader Integer height) {
        peerService.savePeer(port, ipReq);
        try {
            return ResponseEntity.ok().body(blockService.storeAndBroadcastBlock(block, height, ipReq));
        } catch (Exception exception) {
            return ResponseEntity.status(400).body(exception.getMessage());
        }
    }

    @GetMapping("/blocks/height")
    public ResponseEntity<?> getBlockchainHeight(@RequestHeader(required = false) Integer port, HttpServletRequest ipReq) {
        peerService.savePeer(port, ipReq);
        return ResponseEntity.ok().body(blockService.getBlockchainHeight());
    }

    @GetMapping("/blocks")
    public ResponseEntity<?> getAllBlocks(@RequestHeader(required = false) Integer height, @RequestHeader(required = false) Integer port,
                                          HttpServletRequest ipReq, @RequestParam(required = false) Integer heightGetOneBlock) {
        if (port != null) {
            peerService.savePeer(port, ipReq);
        }
        if (heightGetOneBlock != null) {
            return ResponseEntity.ok().body(blockService.findBlockByHeight(heightGetOneBlock));
        }
        if (height == null) {
            return ResponseEntity.ok().body(blockService.findAll());
        } else {
            return ResponseEntity.ok().body(blockService.getBlocksOverHeight(height, ipReq));
        }
    }

    @GetMapping("/blocks/work")
    public ResponseEntity<?> getWork(@RequestHeader(required = false) Integer port, HttpServletRequest ipreq) {
        peerService.savePeer(port, ipreq);
        return ResponseEntity.ok().body(blockService.getWork());
    }

    @GetMapping("/updates")
    public SseEmitter getServerSentEvents() {
        return blockService.addEmitter();
    }
}
