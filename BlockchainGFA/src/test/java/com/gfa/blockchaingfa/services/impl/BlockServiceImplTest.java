package com.gfa.blockchaingfa.services.impl;

import com.gfa.blockchaingfa.models.Block;
import com.gfa.blockchaingfa.models.BlockData;
import com.gfa.blockchaingfa.repositories.BlockRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BlockServiceImplTest {
    /*
    @Mock
    private BlockRepository blockRepository;
    @InjectMocks
    private BlockServiceImpl blockService;
    private Block validBlock;

    @BeforeEach
    void setUp() {
        validBlock = spy(Block.builder().hash("0000hash block").blockData(new BlockData())
                .previousHash("0000previous hash block").timestamp(9999)
                .nonce(10).build());
    }


    @Test
    void findAll() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(validBlock);
        when(blockRepository.findAll()).thenReturn(blocks);
        List<Block> result = (List<Block>) blockService.findAll();
        assertEquals(result.size(), 1);
    }

    @Test
    void findByHash() {
        when(blockRepository.findById("hash block")).thenReturn(Optional.ofNullable(validBlock));
        when(blockRepository.findById("non-existing hash")).thenReturn(Optional.empty());
        Block resultExistingBlock = blockService.findByHash("hash block");
        Assertions.assertThat(resultExistingBlock).isEqualTo(validBlock);
        assertThrows(RuntimeException.class, () -> blockService.findByHash("non-existing hash"));
    }

    @Test
    void isBlockValid() throws IOException {
        Block previousBlock = Block.builder().hash("0000previous hash block").blockData(new BlockData())
                .previousHash("0000previous previous hash block").timestamp(1111)
                .nonce(10).build();
        Block nonValidHashBlock = spy(Block.builder().hash("hash block").blockData(new BlockData())
                .previousHash("0000previous hash block").timestamp(8888)
                .nonce(10).build());
        Block nonValidTimeStampBlock = spy(Block.builder().hash("0000previous hash block").blockData(new BlockData())
                .previousHash("0000previous hash block").timestamp(100)
                .nonce(10).build());
        when(blockRepository.findTopByOrderByTimestampDesc()).thenReturn(previousBlock);
        doReturn("0000hash block").when(validBlock).calculateHash();
        doReturn("hash block").when(nonValidHashBlock).calculateHash();
        doReturn("0000previous hash block").when(nonValidTimeStampBlock).calculateHash();
        assertTrue(blockService.isBlockValid(validBlock));
        assertFalse(blockService.isBlockValid(nonValidTimeStampBlock));
        blockService.setDifficulty(3);
        assertFalse(blockService.isBlockValid(nonValidHashBlock));
    }

     */
}