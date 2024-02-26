package com.gfa.blockchaingfa.services;

import com.gfa.blockchaingfa.models.Block;
import jakarta.servlet.http.HttpServletRequest;

public interface ApiService {
    void broadcastBlock(Block block, Integer height);

    Block receiveBlockByHeight(HttpServletRequest ipReq, Integer height);
}
