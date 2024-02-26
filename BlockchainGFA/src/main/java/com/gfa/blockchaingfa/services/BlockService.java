package com.gfa.blockchaingfa.services;

import com.gfa.blockchaingfa.models.Block;
import com.gfa.blockchaingfa.models.Work;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.List;
import java.util.Map;


public interface BlockService {
    Iterable<Block> findAll();

    Block storeAndBroadcastBlock(Block receivedMinedBlock, Integer height, HttpServletRequest ipReq) throws IOException;

    Block findByHash(String hash);

    Block mine();

    Block storeBlock(Block block, Integer height, HttpServletRequest ipReq) throws IOException;

    Integer getBlockchainHeight();

    List<Block> getBlocksOverHeight(Integer height, HttpServletRequest ipReq);

    Block findBlockByHeight(Integer height);

    Work getWork();

    void updateMinerStopper(boolean minerStopper);

    void sendMinerStopperToClients(boolean minerStopper);

    SseEmitter addEmitter();

    Integer findHeightOfHighestCommonBlock(HttpServletRequest ipReq);
}

