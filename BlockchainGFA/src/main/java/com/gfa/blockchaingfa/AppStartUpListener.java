package com.gfa.blockchaingfa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfa.blockchaingfa.models.Block;
import com.gfa.blockchaingfa.models.Peer;
import com.gfa.blockchaingfa.repositories.BlockRepository;
import com.gfa.blockchaingfa.repositories.PeerRepository;
import com.gfa.blockchaingfa.services.impl.BlockServiceImpl;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

import reactor.core.publisher.Mono;

@Component
public class AppStartUpListener implements ApplicationListener<ContextRefreshedEvent> {
    private final BlockRepository blockRepository;
    private final PeerRepository peerRepository;
    private final BlockServiceImpl blockService;
    private final WebClient webclient = WebClient.builder().build();
    private final ServerProperties serverProperties;
    private final List<Peer> peersTriedFetchingBlocksFrom;

    public AppStartUpListener(BlockRepository blockRepository, PeerRepository peerRepository, BlockServiceImpl blockService, ServerProperties serverProperties, List<Peer> peersTriedFetchingBlocksFrom) {
        this.blockRepository = blockRepository;
        this.peerRepository = peerRepository;
        this.blockService = blockService;
        this.serverProperties = serverProperties;
        this.peersTriedFetchingBlocksFrom = peersTriedFetchingBlocksFrom;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (blockRepository.findAll().isEmpty()) {
            blockRepository.save(new Block("0", null, "0", 0, 0));
        }
        int port = serverProperties.getPort();
        Thread thread = new Thread(() -> {
            getPeers(port);
            List<Peer> peers = peerRepository.findAll();
            getBlocksFromPeerWithMostBlocks(peers, port);
        });
        thread.start();
    }

    public List<Peer> getPeers(int port) {
        try {
            String serverAddress = "https://blockchain.gfapp.eu/api/peers";
            String responseBody = webclient.get()
                    .uri(serverAddress)
                    .header("port", String.valueOf(port))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            ObjectMapper objectMapper = new ObjectMapper();
            List<Peer> peers;
            peers = objectMapper.readValue(responseBody, new TypeReference<>() {
            });
            return (List<Peer>) peerRepository.saveAll(peers);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Block> getBlocksFromPeerWithMostBlocks(List<Peer> peers, int port) {
        Map<Peer, Integer> peerToMap = getPeersHeight(peers, port);
        int peerHeight = Integer.MIN_VALUE;
        Peer peerWithMostBlocks = new Peer();
        for (Map.Entry<Peer, Integer> peer : peerToMap.entrySet()) {
            if (peer.getValue() > peerHeight) {
                peerHeight = peer.getValue();
                peerWithMostBlocks = peer.getKey();
            }
        }
        return getBlocksFromTopPeer(peerHeight, peerWithMostBlocks, port);
    }

    public Map<Peer, Integer> getPeersHeight(List<Peer> peers, int port) {
        try {
            return Flux.fromIterable(peers)
                    .flatMap(peer -> webclient.get()
                            .uri(peer.dejPath() + "/api/blocks/height")
                            .header("port", String.valueOf(port))
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(response -> {
                                int height = Integer.parseInt(response);
                                return new AbstractMap.SimpleEntry<>(peer, height);
                            })
                            .onErrorResume(WebClientRequestException.class, e -> {
                                System.err.println("Peer " + peer.getIpAddress() + ":" + peer.getPort() + " is offline, can't fetch height");
                                return Mono.empty();
                            }).timeout(Duration.ofSeconds(2))
                            .onErrorResume(TimeoutException.class, e -> {
                                System.err.println("Peer " + peer.getIpAddress() + ":" + peer.getPort() + " timed out");
                                return Mono.empty();
                            }).onErrorResume(RuntimeException.class, e -> {
                                System.err.println("Request canceled");
                                return Mono.empty();
                            })
                    )
                    .collectMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)
                    .block();
        } catch (WebClientRequestException e) {
            System.err.println("WebClient request failed: " + e.getMessage());
            return Collections.emptyMap();

        } catch (RuntimeException e) {
            System.err.println("Peer timedout");
            return Collections.emptyMap();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return Collections.emptyMap();
        }
    }


    List<Block> getBlocksFromTopPeer(Integer peerHeight, Peer peerWithMostBlocks, int port) {
        Integer neededBlocks = Math.toIntExact(blockRepository.count());
        String responseBody = webclient.get().uri(peerWithMostBlocks.dejPath() + "/api/blocks")
                .header("height", String.valueOf(neededBlocks))
                .header("port", String.valueOf(port))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Block> blocks;
        try {
            blocks = objectMapper.readValue(responseBody, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (Block block : blocks) {
            block.getBlockData().getTransactions().sort((tx1, tx2) -> Long.compare(tx2.getTimestamp(), tx1.getTimestamp()));
        }
        if (isBlockchainValid(blocks)) {
            for (Block block : blocks) {
                try {
                    blockService.storeBlock(block, (int) (blockRepository.count() + 1), null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return (List<Block>) blockRepository.saveAll(blocks);
        } else if (peerWithMostBlocks.getIpAddress() != null) {//so that our server isn't stuck in a loop
            if (!peersTriedFetchingBlocksFrom.contains(peerWithMostBlocks)) {//avoid recursion if we already tried to fetch the peer
                peersTriedFetchingBlocksFrom.add(peerWithMostBlocks);
                getPeers(port);
                List<Peer> peers = peerRepository.findAll();
                return getBlocksFromPeerWithMostBlocks(peers, port);
            }
        } else {
            System.err.println("All peers are offline, can't get blocks");
        }
        return Collections.emptyList();
    }

    public boolean isBlockchainValid(List<Block> blocks) {
        for (int i = 0; i < blocks.size(); i++) {
            if (i == 0) {
                if (!Objects.equals(blocks.get(i).getHash(), blocks.get(i).calculateHash())
                        || !Objects.equals(blocks.get(i).getPreviousHash(), blockRepository.findTopByOrderByTimestampDesc().getHash())) {
                    return false;
                }
            } else {
                if (!Objects.equals(blocks.get(i).calculateHash(), blocks.get(i).getHash())
                        || !Objects.equals(blocks.get(i).getPreviousHash(), blocks.get(i - 1).getHash())) {
                    return false;
                }
            }
        }
        return true;
    }
}
