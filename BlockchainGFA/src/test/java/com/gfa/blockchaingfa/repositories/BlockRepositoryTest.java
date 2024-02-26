package com.gfa.blockchaingfa.repositories;

import com.gfa.blockchaingfa.models.Block;
import com.gfa.blockchaingfa.models.BlockData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
class BlockRepositoryTest {
    @Autowired
    private BlockRepository blockRepository;
    /*
    @BeforeEach
    void setUp() {
        BlockData blockData1 = BlockData.builder().hash("blockData1").build();
        BlockData blockData2 = BlockData.builder().hash("blockData2").build();
        BlockData blockData3 = BlockData.builder().hash("blockData3").build();
        BlockData blockData4 = BlockData.builder().hash("blockData4").build();
        BlockData blockData5 = BlockData.builder().hash("blockData5").build();

        Block block1 = Block.builder().hash("hash1").blockData(blockData1)
                .previousHash("hash0").timestamp(100).nonce(1).build();
        Block block2 = Block.builder().hash("hash2").blockData(blockData2)
                .previousHash("hash1").timestamp(200).nonce(1).build();
        Block block3 = Block.builder().hash("hash3").blockData(blockData3)
                .previousHash("hash2").timestamp(300).nonce(1).build();
        Block block4 = Block.builder().hash("hash4").blockData(blockData4)
                .previousHash("hash3").timestamp(400).nonce(1).build();
        Block block5 = Block.builder().hash("hash5").blockData(blockData5)
                .previousHash("hash4").timestamp(500).nonce(1).build();
        blockRepository.saveAll(List.of(block1,block2, block3, block4, block5));
    }

    @Test
    void findBlocksOverHeightOrderedByTimestampWithLimit() {
        List<Block> foundBlocks = blockRepository.findBlocksOverHeightOrderedByTimestampWithLimit(1, 10);
        List<Block> expectedBlocks = new ArrayList<>(List.of(blockRepository.findById("hash2").get(), blockRepository.findById("hash3").get(), blockRepository.findById("hash4").get(), blockRepository.findById("hash5").get()));
        Assertions.assertThat(foundBlocks).isEqualTo(expectedBlocks);

       List<Block> foundBlocks2 = blockRepository.findBlocksOverHeightOrderedByTimestampWithLimit(6, 10);
       Assertions.assertThat(foundBlocks2.size()).isEqualTo(0);
    }

    @Test
    void findBlockByHeight() {
        Block foundBlock = blockRepository.findBlockByHeight(1);
        Block expectedBlock = blockRepository.findById("hash2").get();
        Assertions.assertThat(foundBlock).isEqualTo(expectedBlock);

        Block foundBlock2 = blockRepository.findBlockByHeight(4);
        Block expectedBlock2 = blockRepository.findById("hash5").get();
        Assertions.assertThat(foundBlock2).isEqualTo(expectedBlock2);

        Block foundBlock3 = blockRepository.findBlockByHeight(5);
        Assertions.assertThat(foundBlock3).isEqualTo(null);
    }

    @Test
    void deleteAllBlocksOverTimestamp() {
        blockRepository.deleteAllBlocksOverTimestamp(200);
        List<Block> foundBlocks = blockRepository.findAll();
        List<Block> expectedBlocks = new ArrayList<>(List.of(blockRepository.findById("hash1").get(), blockRepository.findById("hash2").get()));
        Assertions.assertThat(expectedBlocks).isEqualTo(foundBlocks);
        Assertions.assertThat(expectedBlocks.size()).isEqualTo(2);

    }

     */
}