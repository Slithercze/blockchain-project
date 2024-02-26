package com.gfa.blockchaingfa.models;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BlockTest {
    /*

    @Test
    void calculateHash() {
        // arrange:
        BlockData blockData = BlockData.builder()
                .hash("09e9c78aafd3a25da023ab63d0012bf2856633d1611345dcce5f7cd357d8680f")
                .data("test No. 1")
                .timestamp(1689151313)
                .build();
        Block block = Block.builder()
                .blockData(blockData)
                .previousHash("09e9c78aafd3a25da023ab63d0012bf2856633d1611345dcce5f7cd357d8680f")
                .timestamp(1689151313)
                .build();
        // act:
        String receivedHashOfBlock = block.calculateHash();
        String expectedHashOfBlock = "de19bd3bba241aaef256a20c8a9f0a4f28443ad8e34ae3c5a62a20a866cd1f03";
        // assertion:
        Assertions.assertThat(receivedHashOfBlock).isEqualTo(expectedHashOfBlock);
    }

     */
}