package com.gfa.blockchaingfa.services.impl;

import com.gfa.blockchaingfa.models.*;
import com.gfa.blockchaingfa.repositories.BlockRepository;
import com.gfa.blockchaingfa.services.BlockService;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.*;

@Service
@Data
public class BlockServiceImpl implements BlockService {

    @Value("${DIFFICULTY}")
    private int difficulty;
    @Value("${LIMIT_FOR_SENDING_BLOCKS}")
    private int limit;
    private String prefix;
    private boolean minerStopper = false;
    private final BlockRepository blockRepository;
    private final ApiServiceImpl apiService;
    private List<SseEmitter> emitters;
    private boolean isMiningOn = false;
    private final TransactionServiceImpl transactionService;

    @Autowired
    public BlockServiceImpl(BlockRepository blockRepository, ApiServiceImpl apiService, TransactionServiceImpl transactionService) {
        this.blockRepository = blockRepository;
        this.apiService = apiService;
        this.emitters = new ArrayList<>();
        this.transactionService = transactionService;
    }

    public Block storeAndBroadcastBlock(Block receivedMinedBlock, Integer height, HttpServletRequest ipReq) throws IOException {
        if (!blockRepository.existsById(receivedMinedBlock.getHash())) {
            storeBlock(receivedMinedBlock, height, ipReq);
            Thread thread = new Thread(() -> {
                apiService.broadcastBlock(receivedMinedBlock, height);
            });
            thread.start();
            return receivedMinedBlock;
        }
        return null;
    }

    public List<Block> findAll() {
        return blockRepository.findAll();
    }

    public Block findByHash(String hash) {
        Optional<Block> block = blockRepository.findById(hash);
        if (block.isPresent()) {
            return block.get();
        } else {
            throw new RuntimeException("Block not found");
        }
    }

    public Block findLatestBlock() {
        return blockRepository.findTopByOrderByTimestampDesc();
    }


    public Block mine() {
        if (isMiningOn) {
            throw new RuntimeException("There is ongoing mining!");
        }
        isMiningOn = true;
        prefix = "0".repeat(difficulty);
        Block block = new Block();
        BlockData blockData = new BlockData("Hello World");
        block.setPreviousHash(findLatestBlock().getHash());

        blockData.setHash();
        block.setBlockData(blockData);
        block.setTimestamp(System.currentTimeMillis() / 1000L);
        block.setHash(block.calculateHash());

        long t = System.currentTimeMillis() / 1000L;
        int lastNonce = 0;

        while (!block.getHash().substring(0, difficulty).equals(prefix) && !minerStopper) {
            // We use replace('\0', '0') to replace null characters ('\0') in the char array with '0'
            block.setNonce(block.getNonce() + 1);
            block.setTimestamp(System.currentTimeMillis() / 1000L);
            block.setHash(block.calculateHash());

            if (block.getTimestamp() != t) {
                System.out.println(block.getNonce());
                System.out.println(block.getNonce() - lastNonce + " hashes per second");

                lastNonce = block.getNonce();
                t = block.getTimestamp();
            }
        }
        minerStopper = false;
        isMiningOn = false;
        if (block.getHash().substring(0, difficulty).equals(prefix)) {
            updateMinerStopper(true);
            blockRepository.save(block);
            apiService.broadcastBlock(block, (int) blockRepository.count());
            return block;
        } else {
            throw new RuntimeException("New block received! (from other peer)");
        }
    }

    public Block storeBlock(Block receivedMinedBlock, Integer height, HttpServletRequest ipReq) throws IOException {
        if (isBlockValid(receivedMinedBlock) && height == getBlockchainHeight() + 1 && !blockRepository.existsById(receivedMinedBlock.getHash())) {
            minerStopper = true;
            updateMinerStopper(true);
            List<Transaction> transactionsFromBlock = receivedMinedBlock.getBlockData().getTransactions();
            List<Transaction> sortedTransactions = new ArrayList<>();
            sortedTransactions.addAll(transactionsFromBlock);
            System.out.println("calling saveBlock()");
            receivedMinedBlock.getBlockData().setTransactions(sortedTransactions);
            blockRepository.save(receivedMinedBlock);
            System.out.println("Block saved");
            minerStopper = false;
            return receivedMinedBlock;
        } else if (height > getBlockchainHeight()) {
            getAllMissingBlocksUpToReceivedOne(ipReq);
            return receivedMinedBlock;
        } else {
            throw new InvalidObjectException("Invalid block");
        }
    }


    public boolean isBlockValid(Block block) throws IOException {
        block.getBlockData().getTransactions().sort((tx1, tx2) -> Long.compare(tx2.getTimestamp(), tx1.getTimestamp()));
        if (transactionService.validateTransactions(block.getBlockData().getTransactions())) {
            Gson gson = new Gson(); // Create new Gson instance
            String jsonBlock = gson.toJson(block);
            System.out.println(jsonBlock);
            prefix = "0".repeat(difficulty);
            return block.calculateHash().substring(0, difficulty).equals(prefix) && isTimeStampValid(block) && isPreviousHashValid(block) && Objects.equals(block.calculateHash(), block.getHash());
        }
        return false;
    }


    public boolean isTimeStampValid(Block block) {
        return findLatestBlock().getTimestamp() < block.getTimestamp();
    }

    public boolean isPreviousHashValid(Block block) {
        return Objects.equals(findLatestBlock().getHash(), block.getPreviousHash());
    }

    public Integer getBlockchainHeight() {
        return (int) blockRepository.count();
    }

    public List<Block> getBlocksOverHeight(Integer height, HttpServletRequest ipReq) {
        return blockRepository.findBlocksOverHeightOrderedByTimestampWithLimit(height, limit);
    }

    public void getAllMissingBlocksUpToReceivedOne(HttpServletRequest ipReq) throws IOException {
        List<Block> missingBlocks = apiService.receiveBlocksOverHeight(ipReq, (int) blockRepository.count());
        Block firstReceivedBlock = missingBlocks.get(0);
        // if there is NO fork in the blockchain:
        if (isBlockValid(firstReceivedBlock)) {
            minerStopper = true;
            updateMinerStopper(true);
            for (Block block : missingBlocks) {
                if (isBlockValid(block)) {
                    blockRepository.save(block);
                } else {
                    break;
                }
            }
            // if there IS fork in the blockchain:
        } else {
            int heightOfHighestCommonBlock = findHeightOfHighestCommonBlock(ipReq);
            if (heightOfHighestCommonBlock >= 1) {
                missingBlocks = apiService.receiveBlocksOverHeight(ipReq, heightOfHighestCommonBlock);
                List<Block> deletedBlocks = blockRepository.findBlocksOverHeightOrderedByTimestampWithLimit(heightOfHighestCommonBlock, (int) blockRepository.count());
                deleteBlocksOverTime(deletedBlocks, (blockRepository.getTimestampOfBlockByHeight(heightOfHighestCommonBlock - 1))); //offset in query is excluded! in implementation => searched height must be -1!
                boolean isEveryBlockValid = true;
                for (Block block : missingBlocks) {
                    if (!isBlockValid(block)) {
                        isEveryBlockValid = false;
                        List<Block> newBlocksToBeDeleted = blockRepository.getBlocksOverHeight(heightOfHighestCommonBlock);
                        deleteBlocksOverTime(deletedBlocks, (blockRepository.getTimestampOfBlockByHeight(heightOfHighestCommonBlock - 1))); //offset in query is excluded! in implementation => searched height must be -1!
                        saveListOfBlocks(deletedBlocks);
                        break;
                    } else {
                        blockRepository.save(block);
                    }
                }
                if (isEveryBlockValid) {
                    minerStopper = true;
                    updateMinerStopper(true);
                }
            }
        }
        minerStopper = false;
        updateMinerStopper(false);
    }

    public void saveListOfBlocks(List<Block> blocks) {
        blockRepository.saveAll(blocks);
    }

    public Integer findHeightOfHighestCommonBlock(HttpServletRequest ipReq) {
        int left = 1;
        int right = (int) blockRepository.count();
        int mid = 0;


        while (left != right) {
            mid = (right + left) / 2;
            String otherMidBlockHash = apiService.receiveBlockByHeight(ipReq, mid).getHash();
            String myMidBlockHash = blockRepository.findBlockByHeight(mid - 1).getHash(); // (mid - 1) => because in blockRepository.findBlockByHeight() is offset excluded
            if (otherMidBlockHash.equals(myMidBlockHash)) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        mid = (right + left) / 2;
        String otherMidBlockHash = apiService.receiveBlockByHeight(ipReq, mid).getHash();
        String myMidBlockHash = blockRepository.findBlockByHeight(mid - 1).getHash();
        if (otherMidBlockHash.equals(myMidBlockHash)) {
            return mid;
        } else return mid - 1;
    }

    public Block findBlockByHeight(Integer height) {
        return blockRepository.findBlockByHeight(height - 1);
    }

    @Override
    public Work getWork() {
        Block block = blockRepository.findTopByOrderByTimestampDesc();
        Work work = new Work();
        work.setDifficulty(String.valueOf(difficulty));
        work.setHeight(String.valueOf(blockRepository.count() + 1));
        work.setPreviousHash(block.getHash());
        work.setTxList(transactionService.calculateMempool());
        return work;
    }

    public SseEmitter addEmitter() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);
        return emitter;
    }

    public void updateMinerStopper(boolean minerStopper) {
        sendMinerStopperToClients(minerStopper);
    }

    public void sendMinerStopperToClients(boolean minerStopper) {
        List<SseEmitter> toRemove = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("minerStopperUpdate").data(minerStopper));
            } catch (Exception e) {
                toRemove.add(emitter);
            }
        }
        emitters.removeAll(toRemove);
    }

    private void deleteBlocksOverTime(List<Block> deletedBlocks, Integer timestampOfBlockByHeight) {
        for (Block block : deletedBlocks) {
            if (block.getTimestamp() > timestampOfBlockByHeight) {
                blockRepository.delete(block);
            }
        }
    }
}
