package com.gfa.blockchaingfa.services.impl;

import com.gfa.blockchaingfa.models.Block;
import com.gfa.blockchaingfa.models.BlockData;
import com.gfa.blockchaingfa.repositories.BlockRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class BlockServiceImplIntegrationTests {
    @Autowired
    private BlockRepository blockRepository;
    @MockBean
    private ApiServiceImpl apiService;
    @Autowired
    private BlockServiceImpl blockService;
    /*

    @Test
    void findHeightOfHighestCommonBlock() {
        List<Block> myBlockChain = new ArrayList<>();
        BlockData blockData1 = new BlockData("blockData1");
        blockData1.setHash();
        BlockData blockData2 = new BlockData("blockData2");
        blockData2.setHash();
        BlockData blockData3 = new BlockData("blockData3");
        blockData3.setHash();
        BlockData blockData4 = new BlockData("blockData4");
        blockData4.setHash();
        BlockData blockData5 = new BlockData("blockData5");
        blockData5.setHash();
        BlockData blockData6 = new BlockData("blockData6");
        blockData6.setHash();
        BlockData blockData7 = new BlockData("blockData7");
        blockData7.setHash();
        BlockData blockData8 = new BlockData("blockData8");
        blockData8.setHash();
        myBlockChain.add(Block.builder().hash("hash1").blockData(blockData1).previousHash("myHash0").timestamp(1001).nonce(10).build());
        myBlockChain.add(Block.builder().hash("hash2").blockData(blockData2).previousHash("myHash1").timestamp(1002).nonce(10).build());
        myBlockChain.add(Block.builder().hash("hash3").blockData(blockData3).previousHash("myHash2").timestamp(1003).nonce(10).build());
        myBlockChain.add(Block.builder().hash("hash4").blockData(blockData4).previousHash("myHash3").timestamp(1004).nonce(10).build());
        myBlockChain.add(Block.builder().hash("hash5").blockData(blockData5).previousHash("myHash4").timestamp(1005).nonce(10).build());
        myBlockChain.add(Block.builder().hash("hash6").blockData(blockData6).previousHash("myHash5").timestamp(1006).nonce(10).build());
        myBlockChain.add(Block.builder().hash("hash7").blockData(blockData7).previousHash("myHash6").timestamp(1007).nonce(10).build());
        myBlockChain.add(Block.builder().hash("hash8").blockData(blockData8).previousHash("myHash7").timestamp(1008).nonce(10).build());
        blockRepository.saveAll(myBlockChain);

        List<Block> otherBlockChain = new ArrayList<>();
        otherBlockChain.add(Block.builder().hash("hash1").blockData(new BlockData()).previousHash("otherHash0").timestamp(1001).nonce(10).build());
        otherBlockChain.add(Block.builder().hash("hash2").blockData(new BlockData()).previousHash("otherHash1").timestamp(1002).nonce(10).build());
        otherBlockChain.add(Block.builder().hash("hash3").blockData(new BlockData()).previousHash("otherHash2").timestamp(1003).nonce(10).build());
        otherBlockChain.add(Block.builder().hash("hash4").blockData(new BlockData()).previousHash("otherHash3").timestamp(1004).nonce(10).build());
        otherBlockChain.add(Block.builder().hash("hash5").blockData(new BlockData()).previousHash("otherHash4").timestamp(1005).nonce(10).build());
        otherBlockChain.add(Block.builder().hash("hash60").blockData(new BlockData()).previousHash("otherHash5").timestamp(1006).nonce(10).build());
        otherBlockChain.add(Block.builder().hash("hash70").blockData(new BlockData()).previousHash("otherHash6").timestamp(1007).nonce(10).build());
        otherBlockChain.add(Block.builder().hash("hash80").blockData(new BlockData()).previousHash("otherHash7").timestamp(1008).nonce(10).build());

        HttpServletRequest ipReq = new MockHttpServletRequest();

        when(apiService.receiveBlockByHeight(ipReq, 1)).thenReturn(otherBlockChain.get(0));
        when(apiService.receiveBlockByHeight(ipReq, 2)).thenReturn(otherBlockChain.get(1));
        when(apiService.receiveBlockByHeight(ipReq, 3)).thenReturn(otherBlockChain.get(2));
        when(apiService.receiveBlockByHeight(ipReq, 4)).thenReturn(otherBlockChain.get(3));
        when(apiService.receiveBlockByHeight(ipReq, 5)).thenReturn(otherBlockChain.get(4));
        when(apiService.receiveBlockByHeight(ipReq, 6)).thenReturn(otherBlockChain.get(5));
        when(apiService.receiveBlockByHeight(ipReq, 7)).thenReturn(otherBlockChain.get(6));
        when(apiService.receiveBlockByHeight(ipReq, 8)).thenReturn(otherBlockChain.get(7));

        Integer foundHeight = blockService.findHeightOfHighestCommonBlock(ipReq);
        Assertions.assertThat(foundHeight).isEqualTo(5);
    }

     */
}
