package com.gfa.blockchaingfa.repositories;

import com.gfa.blockchaingfa.models.Block;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Transactional
@Repository
public interface BlockRepository extends CrudRepository<Block, String> {
    Block findTopByOrderByTimestampDesc();

    @Query(value = "SELECT * FROM blocks ORDER BY timestamp LIMIT :limit OFFSET :height ", nativeQuery = true)
    List<Block> findBlocksOverHeightOrderedByTimestampWithLimit(@Param("height") Integer number, @Param("limit") int limit);

    @Override
    List<Block> findAll();

    @Query(value = "SELECT * FROM blocks ORDER BY timestamp LIMIT 1 OFFSET :height", nativeQuery = true)
    Block findBlockByHeight(@Param("height") Integer height);

    @Modifying
    @Query(value = "DELETE FROM blocks WHERE timestamp > :timestamp", nativeQuery = true)
    void deleteAllBlocksOverTimestamp(@Param("timestamp") Integer timestamp);

    @Query(value = "SELECT timestamp FROM blocks ORDER BY timestamp LIMIT 1 OFFSET :height", nativeQuery = true)
    Integer getTimestampOfBlockByHeight(@Param("height") Integer height); //offset is excluded! in implementation => searched height must be -1!

    @Query(value = "SELECT * FROM blocks ORDER BY timestamp OFFSET :height", nativeQuery = true)
    List<Block> getBlocksOverHeight(@Param("height") Integer height);
}
