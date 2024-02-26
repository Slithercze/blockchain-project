package com.gfa.blockchaingfa.repositories;
import com.gfa.blockchaingfa.models.Block;
import com.gfa.blockchaingfa.models.BlockData;
import com.gfa.blockchaingfa.models.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
public class DeletingCascadeTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BlockRepository blockRepository;
    /*
    @Test
    @DirtiesContext // Resets the context after the test, ensuring a clean environment
    public void testDeleteBlockCascades() {
        // Create and persist a Block with associated BlockData and Transaction
        Block block = createSampleBlock();
        entityManager.persist(block);

        // Delete the block using the repository method
        blockRepository.delete(block);

        // Verify that all related entities are deleted
        assertNull(entityManager.find(Block.class, block.getHash()));
        assertNull(entityManager.find(BlockData.class, block.getBlockData().getHash()));
        assertNull(entityManager.find(Transaction.class, block.getBlockData().getTransactions().get(0).getHash()));
    }


    private Block createSampleBlock() {
        List<Transaction> transactions = new ArrayList<>();
        BlockData blockData = new BlockData("blockData1");
        transactions.add(new Transaction("transactionHash1", 1, "to", 1,  "from", "signature", blockData));
        blockData.setTransactions(transactions);
        blockData.setHash("blockDataHash1");

        // Set block properties...
        // Create associated BlockData and Transaction...
        return new Block("block1", blockData, "previousHashBlock1", 1L, 1);
    }

     */
}
