package com.gfa.blockchaingfa.services;

import com.gfa.blockchaingfa.models.Peer;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface PeerService {
    List<Peer> findTenPeers();
    void savePeer(Integer port, HttpServletRequest ipreq);
}
