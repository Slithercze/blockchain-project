package com.gfa.blockchaingfa.services.impl;

import com.gfa.blockchaingfa.models.Peer;
import com.gfa.blockchaingfa.repositories.PeerRepository;
import com.gfa.blockchaingfa.services.PeerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class PeerServiceImpl implements PeerService {
    private final PeerRepository peerRepository;

    public PeerServiceImpl(PeerRepository peerRepository) {

        this.peerRepository = peerRepository;
    }

    public List<Peer> findTenPeers() {
        return peerRepository.findTenLastActivePeers();
    }

    public void savePeer(Integer port, HttpServletRequest ipReq) {
        if (port == null) {
            return;
        }
        String ip = ipReq.getRemoteAddr();
        if (!peerRepository.existsById(ip) || peerRepository.existsById(ip) && port != peerRepository.findById(ip).get().getPort()) {
            peerRepository.save(new Peer(ip, port, new Timestamp(System.currentTimeMillis())));
        } else if (peerRepository.existsById(ip) && port == peerRepository.findById(ip).get().getPort()) {
            Peer peer = peerRepository.findById(ip).get();
            peer.setLastActive(new Timestamp(System.currentTimeMillis()));
            peerRepository.save(peer);
        }
    }
}
