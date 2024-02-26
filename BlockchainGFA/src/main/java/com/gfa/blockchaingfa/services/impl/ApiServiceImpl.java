package com.gfa.blockchaingfa.services.impl;

import com.gfa.blockchaingfa.models.Block;
import com.gfa.blockchaingfa.models.Peer;
import com.gfa.blockchaingfa.models.Transaction;
import com.gfa.blockchaingfa.repositories.PeerRepository;
import com.gfa.blockchaingfa.services.ApiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ApiServiceImpl implements ApiService {
    private final PeerRepository peerRepository;
    private final WebClient webclient = WebClient.builder().build();
    private final ServerProperties serverProperties;

    @Autowired
    public ApiServiceImpl(PeerRepository peerRepository, ServerProperties serverProperties) {
        this.peerRepository = peerRepository;
        this.serverProperties = serverProperties;
    }

    @Override
    public void broadcastBlock(Block block, Integer height) {
        List<Peer> peers = peerRepository.findAll();
        int port = serverProperties.getPort();

        Flux.fromIterable(peers)
                .flatMap(peer -> webclient.post()
                        .uri("http://" + peer.getIpAddress() + ":" + peer.getPort() + "/api/blocks")
                        .bodyValue(block)
                        .header("port", String.valueOf(port))
                        .header("height", height.toString())
                        .retrieve()
                        .bodyToMono(String.class)
                        .onErrorResume(WebClientResponseException.class, e -> {
                            if (e.getRawStatusCode() == 400) {
                                System.err.println("Peer " + peer.getIpAddress() + ":" + peer.getPort() + " returned 400 Bad Request while broadcasting block");
                            } else {
                                System.err.println("Error while broadcasting block to peer " + peer.getIpAddress() + ":" + peer.getPort() + ": " + e.getMessage());
                            }
                            return Mono.empty();
                        })
                        .onErrorResume(WebClientRequestException.class, e -> {
                            System.err.println("Peer " + peer.getIpAddress() + ":" + peer.getPort() + " is offline, can't broadcast block");
                            return Mono.empty();
                        }))
                .collectList()
                .block();
    }

    public List<Block> receiveBlocksOverHeight(HttpServletRequest ipReq, Integer height) {
        String ipAddress = ipReq.getRemoteAddr();
        Optional<Peer> whoSendNewBlock = peerRepository.findById(ipAddress);

        return whoSendNewBlock.map(peer -> webclient.get()
                        .uri("http://" + peer.getIpAddress() + ":" + peer.getPort() + "/api/blocks")
                        .header("height", height.toString())
                        .retrieve()
                        .bodyToFlux(Block.class)
                        .collectList()
                        .block())
                .orElse(Collections.emptyList());
    }

    public Block receiveBlockByHeight(HttpServletRequest ipReq, Integer height) {
        String ipAddress = ipReq.getRemoteAddr();
        Optional<Peer> whoSendNewBlock = peerRepository.findById(ipAddress);
        return whoSendNewBlock.map(peer -> webclient.get()
                        .uri("http://" + peer.getIpAddress() + ":" + peer.getPort() + "/api/blocks?heightGetOneBlock=" + height)
                        .retrieve()
                        .bodyToFlux(Block.class)
                        .next().block())
                .orElse(null);
    }
    public void broadcastTransaction(Transaction newTransaction) {
        System.out.println("enter broadcastTransaction");
        List<Peer> peers = peerRepository.findAll();
        int port = serverProperties.getPort();

        Flux.fromIterable(peers)
                .flatMap(peer -> webclient.post()
                        .uri("http://" + peer.getIpAddress() + ":" + peer.getPort() + "/api/newtransaction")
                        .bodyValue(newTransaction)
                        .header("port", String.valueOf(port))
                        .retrieve()
                        .bodyToMono(String.class)
                        .onErrorResume(WebClientRequestException.class, e -> {
                            System.err.println("Peer " + peer.getIpAddress() + ":" + peer.getPort() + " is offline, can't broadcast transaction");
                            return Mono.empty();
                        }))
                .collectList()
                .block();
        System.out.println("exit broadcastTransaction");
    }
}
